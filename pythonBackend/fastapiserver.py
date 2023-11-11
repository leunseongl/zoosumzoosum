from fastapi import FastAPI, File, Form
from fastapi.responses import JSONResponse
import numpy as np
import cv2
from ultralytics import YOLO
import base64
from typing import Optional
from pydantic import BaseModel

app = FastAPI()

# class name
trash_name = [
        "general trash",
        "paper",
        "metal",
        "glass",
        "plastic",
        "plastic bag"
    ]

color = [
    [255, 0 ,0],
    [0, 255 ,255],
    [102, 51 ,255],
    [102, 255 ,102],
    [255, 255 ,102],
    [255, 102 ,153],
         ]



# 모델 로딩
smodel = YOLO('./yolov8s_custom.pt')
nmodel = YOLO('./yolov8n_custom.pt')
print("model load 완료")

def getResultFromData(
        file,
        leftRightPercent,
        topPercentPercent,
        bottomPercentPercent,
        modelType
):
    # numpy를 image buffer를 array로 변환
    nparr = np.frombuffer(file, np.uint8)

    # 이미지 코드로 변환
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    # 이미지 크기 계산
    h, w, _ = img.shape

    # 혹시 가로로 찍혔다면 좌우를 고려하여 넓게 잡음
    if w > h:
        topPercentPercent = leftRightPercent
        bottomPercentPercent = leftRightPercent

    # 상하좌우 바우더리 제한값 계산
    x1Limit = int(w*leftRightPercent)
    x2Limit = w - int(w*leftRightPercent)
    y1Limit = int(h*topPercentPercent)
    y2Limit = h - int(h*bottomPercentPercent)

    # 예측 실행
    # model
    model = smodel
    if modelType == 'n':
        model = nmodel
    results = model(img)

    # class 별 count 초기화 
    trash_count = [0]*6

    confidences = []
    bboxes = []
    class_ids = []
    for result in results:
        boxes = result.boxes
        for box in boxes:
            confidence = box.conf
            # 0.4 이상의 confidence 만 검출
            if confidence >= 0.4:
                xyxy = box.xyxy.tolist()[0]
                bbox = list(map(int, xyxy)) 
                x1, y1, x2, y2 = bbox

                # 바운더리 초과 시 결과에 포함하지 않음
                if x1 <= x1Limit or y1 <= y1Limit or x2 >= x2Limit or y2 >= y2Limit:
                    continue
                bboxes.append(xyxy)
                confidences.append(float(confidence))

                if box.cls == 0.:
                    class_ids.append(0)
                    trash_count[0] += 1
                elif box.cls == 1. or box.cls == 2.:
                    class_ids.append(1)
                    trash_count[1] += 1
                elif box.cls == 3.:
                    class_ids.append(2)
                    trash_count[2] += 1
                elif box.cls == 4.:
                    class_ids.append(3)
                    trash_count[3] += 1
                elif box.cls == 5.:
                    class_ids.append(4)
                    trash_count[4] += 1
                elif box.cls == 7.:
                    class_ids.append(5)
                    trash_count[5] += 1

    predict_result = {trash_name[i]: trash_count[i] for i in range(len(trash_name))}
    predict_result["total"] = sum(trash_count)
    result = {
        "predict_result" : predict_result,
        "confidences" : confidences,
        "bboxes" : bboxes,
        "class_ids" : class_ids,
        "img" : img
    }
    return result


@app.get("/")
def root():
    return "Ggyumo's fastapi server is running!"


# image upload 후 예측 결과 반환
@app.post('/ai')
def predict(
    file: bytes = File(),
    leftRightPercent : Optional[float] = Form(0.1),
    topPercentPercent : Optional[float] = Form(0.1),
    bottomPercentPercent : Optional[float] = Form(0.3),
    modelType : Optional[str] = Form('s'),
    ):
    return JSONResponse(getResultFromData(file, leftRightPercent, topPercentPercent,bottomPercentPercent, modelType)["predict_result"])

        

# image upload 후 예측 결과 이미지반환
@app.post('/ai/image')
def predict(
    file: bytes = File(),
    leftRightPercent : Optional[float] = Form(0.1),
    topPercentPercent : Optional[float] = Form(0.1),
    bottomPercentPercent : Optional[float] = Form(0.3),
    modelType : Optional[str] = Form('s'),
    ):
        
    result = getResultFromData(file, leftRightPercent, topPercentPercent,bottomPercentPercent, modelType)
    predict_result = result["predict_result"]
    confidences = result["confidences"]
    bboxes = result["bboxes"]
    class_ids = result["class_ids"]
    img = result["img"]

    # cv2 가 제공하는 후처리 모델
    result_boxes = cv2.dnn.NMSBoxes(bboxes, confidences, 0.25, 0.45, 0.5)

    # cv2 글씨 폰트
    # 한글로 수정예정
    font = cv2.FONT_HERSHEY_PLAIN

    # cv2 박스, 글씨 생성
    for i in range(len(bboxes)):
        if i in result_boxes:
            bbox = list(map(int, bboxes[i])) 
            x, y, x2, y2 = bbox
            cv2.rectangle(img, (x, y), (x2, y2), color[class_ids[i]], 2)
            cv2.putText(img, 
                        trash_name[class_ids[i]]
                        , (x, y + 30), font, 2, color[class_ids[i]], 2)

    # cv2 데이터를 base64 로 인코딩
    encode_param=[int(cv2.IMWRITE_JPEG_QUALITY),90]
    _, bts = cv2.imencode('.jpg', img, encode_param)
    encoded = base64.b64encode(bts.tobytes())
    decoded = encoded.decode('ascii')

    result = {
        "predictResult" : predict_result,
        "decodedImage" : decoded,
    }
    return JSONResponse(result)