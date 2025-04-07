package com.kcs3.auction.service;

import com.kcs3.auction.entity.Alarm;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.repository.AlarmRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AuthUserProvider authUserProvider;
    private final AlarmRepository alarmRepository;

    public List<String> loadAlarm() {
        User user = authUserProvider.getCurrentUser();

        List<Alarm> alarms = alarmRepository.findTop4ByUserIdOrderByCreatedAtDesc(user.getUserId());

        return alarms.stream()
            .map(Alarm::getAlarmContent)
            .collect(Collectors.toList());
    }
}
