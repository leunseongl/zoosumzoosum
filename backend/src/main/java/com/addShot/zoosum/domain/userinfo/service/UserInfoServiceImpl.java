package com.addShot.zoosum.domain.userinfo.service;

import com.addShot.zoosum.domain.animal.repository.AnimalMotionRepository;
import com.addShot.zoosum.domain.animal.repository.UserAnimalRepository;
import com.addShot.zoosum.domain.common.repository.BadgeRepository;
import com.addShot.zoosum.domain.common.repository.UserBadgeRepository;
import com.addShot.zoosum.domain.item.repository.ItemRepository;
import com.addShot.zoosum.domain.item.repository.UserItemRepository;
import com.addShot.zoosum.domain.userinfo.dto.response.BadgeListItemResponse;
import com.addShot.zoosum.domain.userinfo.dto.response.MainResponse;
import com.addShot.zoosum.domain.userinfo.dto.response.PlogRecordResponse;
import com.addShot.zoosum.domain.userinfo.dto.response.SelectedAnimalResponse;
import com.addShot.zoosum.domain.userinfo.dto.response.SelectedItemResponse;
import com.addShot.zoosum.domain.userinfo.repository.UserPlogInfoRepository;
import com.addShot.zoosum.entity.Animal;
import com.addShot.zoosum.entity.AnimalMotion;
import com.addShot.zoosum.entity.Badge;
import com.addShot.zoosum.entity.UserAnimal;
import com.addShot.zoosum.entity.UserBadge;
import com.addShot.zoosum.entity.UserItem;
import com.addShot.zoosum.entity.UserPlogInfo;
import com.addShot.zoosum.entity.embedded.UserBadgeId;
import com.addShot.zoosum.entity.enums.ItemType;
import com.addShot.zoosum.util.RandomUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInfoServiceImpl implements UserInfoService {

	private final UserPlogInfoRepository userPlogInfoRepository;
	private final BadgeRepository badgeRepository;
	private final UserBadgeRepository userBadgeRepository;
	private final UserAnimalRepository userAnimalRepository;
	private final AnimalMotionRepository animalMotionRepository;
	private final UserItemRepository userItemRepository;

	@Override
	public MainResponse getUserMain(String userId) {
		List<UserAnimal> userAnimals = userAnimalRepository.findAllSelectedByUserId(userId).get();

		List<SelectedAnimalResponse> animalResponses = new ArrayList<>();
		for (UserAnimal ua : userAnimals) {
			Long animalId = ua.getAnimal().getAnimalId();
			List<AnimalMotion> animalMotions = animalMotionRepository.findByAnimalId(animalId).get();
			AnimalMotion randomMotion = RandomUtil.getRandomElement(animalMotions);

			SelectedAnimalResponse response = SelectedAnimalResponse.builder()
				.animalId(animalId)
				.animalName(ua.getUserAnimalName())
				.fileUrl(randomMotion.getFileUrl())
				.build();
			animalResponses.add(response);
		}
		//섬에 나와있는 동물 리스트 조회

		UserItem islandItem = userItemRepository.findSelectedItem(userId, ItemType.ISLAND);
		//섬 테마 조회

		SelectedItemResponse island = SelectedItemResponse.builder()
			.itemId(islandItem.getItem().getItmeId())
			.itemName(islandItem.getItem().getItemName())
			.itemFileUrl(islandItem.getItem().getFileUrl())
			.build();

		UserItem treeItem = userItemRepository.findSelectedItem(userId, ItemType.TREE);
		//나무 조회

		SelectedItemResponse tree = SelectedItemResponse.builder()
			.itemId(treeItem.getItem().getItmeId())
			.itemName(treeItem.getItem().getItemName())
			.itemFileUrl(treeItem.getItem().getFileUrl())
			.build();

		MainResponse response = MainResponse.builder()
			.animalList(animalResponses)
			.island(island)
			.tree(tree)
			.build();

		return response;
	}

	@Override
	public PlogRecordResponse getPlogRecord(String userId) {
		UserPlogInfo userPlogInfo = userPlogInfoRepository.findById(userId).get();

		PlogRecordResponse response = PlogRecordResponse.builder()
			.plogCount(userPlogInfo.getPlogCount())
			.sumLength(userPlogInfo.getSumPloggingData().getSumLength())
			.sumTime(userPlogInfo.getSumPloggingData().getSumTime())
			.sumTrash(userPlogInfo.getSumPloggingData().getSumTrash())
			.build();

		return response;
	}

	@Override
	public List<BadgeListItemResponse> getUserBadgeList(String userId) {
		List<Badge> all = badgeRepository.findAll();
		List<BadgeListItemResponse> response = new ArrayList<>();

		for (Badge b : all) {
			UserBadgeId id = new UserBadgeId(userId, b.getBadgeId());
			Optional<UserBadge> badge = userBadgeRepository.findById(id);
			boolean isHave = false;
			if (badge.isPresent()) { //사용자에게 존재하는 뱃지라면
				isHave = true;
			}

			BadgeListItemResponse badgeListItemResponse = BadgeListItemResponse.builder()
				.badgeId(b.getBadgeId())
				.userId(userId)
				.badgeName(b.getBadgeName())
				.fileUrl(b.getFileUrl())
				.isHave(isHave)
				.build();

			response.add(badgeListItemResponse);
		}
		return response;
	}
}
