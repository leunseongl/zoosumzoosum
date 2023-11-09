package com.addShot.zoosum.domain.activity.repository;

import static com.addShot.zoosum.entity.QActivityHistory.activityHistory;
import static com.addShot.zoosum.entity.QUser.user;

import com.addShot.zoosum.entity.ActivityHistory;
import com.addShot.zoosum.entity.User;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class ActivityCustomRepositoryImpl implements ActivityCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ActivityCustomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ActivityHistory> findAllByUserNickname(String nickname, Pageable pageable) {
        if (nickname == null) {
            return null;
        }

        User findUser = queryFactory.selectFrom(user).where(user.nickname.eq(nickname)).fetchOne();
        if (findUser == null) {
            return null;
        }

        QueryResults<ActivityHistory> result = queryFactory.selectFrom(activityHistory)
            .where(activityHistory.user.nickname.eq(nickname))
            .orderBy(activityHistory.activityId.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<ActivityHistory> list = result.getResults();
        long total = result.getTotal();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public Optional<ActivityHistory> findByActivityId(Long activityId) {
        return Optional.ofNullable(queryFactory.selectFrom(activityHistory)
            .where(activityHistory.activityId.eq(activityId))
            .fetchOne());
    }
}
