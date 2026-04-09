package org.example.service.booking;

import org.example.common.PageResult;
import org.example.dto.booking.BookingCancelDTO;
import org.example.dto.booking.BookingCheckinDTO;
import org.example.dto.booking.BookingCreateDTO;
import org.example.dto.booking.PaymentPayDTO;
import org.example.vo.booking.AvailableSlotVO;
import org.example.vo.booking.BookingCreateResultVO;
import org.example.vo.booking.BookingDetailVO;
import org.example.vo.booking.BookingListItemVO;
import org.example.vo.booking.CourtSlotsVO;
import org.example.vo.booking.PaymentPayResultVO;

import java.util.List;

/**
 * 预约模块业务（不含分布式锁、消息队列）
 */
public interface BookingService {

    BookingCreateResultVO createBooking(Long userId, BookingCreateDTO dto);

    PaymentPayResultVO payDeposit(Long userId, PaymentPayDTO dto);

    void cancelBooking(Long userId, BookingCancelDTO dto);

    PageResult<BookingListItemVO> pageMyBookings(Long userId, Integer status, Integer pageNum, Integer pageSize);

    BookingDetailVO getBookingDetail(Long userId, Long bookingId);

    void checkin(Long userId, BookingCheckinDTO dto);

    CourtSlotsVO getCourtSlots(Long courtId, String date);

    List<AvailableSlotVO> getAvailableSlots(Long courtId, String date, String startTime, String endTime);
}
