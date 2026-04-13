package org.example.service.booking;

import org.example.dto.admin.TimeSlotBatchDeleteDTO;
import org.example.dto.admin.TimeSlotGenerateDTO;
import org.example.dto.admin.TimeSlotLockDTO;
import org.example.dto.admin.TimeSlotUpdateDTO;
import org.example.vo.admin.TimeSlotBatchDeleteResultVO;
import org.example.vo.admin.TimeSlotGenerateResultVO;
import org.example.vo.admin.TimeSlotListItemVO;

import java.util.Map;

public interface TimeSlotService {

    TimeSlotGenerateResultVO generateTimeSlots(TimeSlotGenerateDTO dto);

    Map<String, Object> getTimeSlotList(Long courtId, Long venueId, String startDate, String endDate, Integer status, Integer page, Integer size);

    void updateTimeSlot(Long slotId, TimeSlotUpdateDTO dto);

    void deleteTimeSlot(Long slotId, Boolean force);

    TimeSlotBatchDeleteResultVO batchDeleteTimeSlots(TimeSlotBatchDeleteDTO dto);

    void lockTimeSlot(Long slotId, TimeSlotLockDTO dto);
}