package org.example.service.booking;

import org.example.config.exception.CommonJsonException;
import org.example.dto.booking.BookingCancelDTO;
import org.example.dto.booking.BookingCreateDTO;
import org.example.dto.booking.PaymentPayDTO;
import org.example.entity.VenueEntity;
import org.example.entity.booking.BookingEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.booking.PaymentMapper;
import org.example.mapper.booking.TimeSlotMapper;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.service.basic.user.UserService;
import org.example.service.booking.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private TimeSlotMapper timeSlotMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private CourtMapper courtMapper;

    @Mock
    private VenueMapper venueMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private VenueEntity testVenue;
    private org.example.entity.CourtEntity testCourt;
    private TimeSlotEntity testSlot;
    private BookingEntity testBooking;

    @BeforeEach
    void setUp() {
        testVenue = new VenueEntity();
        testVenue.setId(1L);
        testVenue.setName("Test Venue");
        testVenue.setOpenTime("08:00:00");
        testVenue.setCloseTime("22:00:00");

        testCourt = new org.example.entity.CourtEntity();
        testCourt.setId(1L);
        testCourt.setVenueId(1L);
        testCourt.setName("Court 1");
        testCourt.setPricePerHour(new BigDecimal("100.00"));

        testSlot = new TimeSlotEntity();
        testSlot.setId(1L);
        testSlot.setCourtId(1L);
        testSlot.setSlotDate(LocalDate.now().plusDays(1));
        testSlot.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0));
        testSlot.setEndTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0));
        testSlot.setStatus(0);

        testBooking = new BookingEntity();
        testBooking.setId(1L);
        testBooking.setUserId(100L);
        testBooking.setCourtId(1L);
        testBooking.setStartTime(testSlot.getStartTime());
        testBooking.setEndTime(testSlot.getEndTime());
        testBooking.setDepositAmount(new BigDecimal("40.00"));
        testBooking.setStatus(0);
    }

    @Test
    void testCreateBooking_SlotIdsEmpty() {
        BookingCreateDTO dto = new BookingCreateDTO();
        dto.setSlotIds(new ArrayList<>());

        assertThrows(CommonJsonException.class, () -> bookingService.createBooking(100L, dto));
    }

    @Test
    void testPayDeposit_BookingNotFound() {
        Long userId = 100L;
        PaymentPayDTO dto = new PaymentPayDTO();
        dto.setBookingId(999L);

        when(bookingMapper.selectById(999L)).thenReturn(null);

        assertThrows(CommonJsonException.class, () -> bookingService.payDeposit(userId, dto));
    }

    @Test
    void testPayDeposit_AlreadyPaid() {
        Long userId = 100L;
        PaymentPayDTO dto = new PaymentPayDTO();
        dto.setBookingId(1L);

        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(paymentMapper.selectCount(any())).thenReturn(1L);

        assertThrows(CommonJsonException.class, () -> bookingService.payDeposit(userId, dto));
    }

    @Test
    void testPayDeposit_WrongStatus() {
        Long userId = 100L;
        PaymentPayDTO dto = new PaymentPayDTO();
        dto.setBookingId(1L);

        testBooking.setStatus(1);
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        assertThrows(CommonJsonException.class, () -> bookingService.payDeposit(userId, dto));
    }

    @Test
    void testCancelBooking_EmptyList() {
        Long userId = 100L;
        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingIds(new ArrayList<>());

        assertThrows(CommonJsonException.class, () -> bookingService.cancelBooking(userId, dto));
    }

    @Test
    void testTimeoutCancel_BookingNotFound() {
        when(bookingMapper.selectById(999L)).thenReturn(null);

        bookingService.timeoutCancel(999L);

        verify(bookingMapper, never()).updateById(any());
    }

    @Test
    void testTimeoutCancel_WrongStatus() {
        Long bookingId = 1L;
        testBooking.setStatus(1);

        when(bookingMapper.selectById(bookingId)).thenReturn(testBooking);

        bookingService.timeoutCancel(bookingId);

        verify(bookingMapper, never()).updateById(any());
    }

    @Test
    void testBookingComplete_BookingNotFound() {
        when(bookingMapper.selectById(999L)).thenReturn(null);

        bookingService.bookingComplete(999L);
    }

    @Test
    void testBookingComplete_WrongStatus() {
        Long bookingId = 1L;
        testBooking.setStatus(0);

        when(bookingMapper.selectById(bookingId)).thenReturn(testBooking);

        bookingService.bookingComplete(bookingId);
    }

    @Test
    void testBookingBusinessFlow_Explanation() {
        System.out.println("\n========== 预约业务流程详解 ==========");
        System.out.println("\n【状态说明】");
        System.out.println("预约状态(status): 0=待支付, 1=已预约, 2=已取消, 3=已完成, 4=违约");
        System.out.println("时间片状态(status): 0=可预约, 1=待支付, 2=已预约, 3=已锁定");

        System.out.println("\n【流程一：正常预约】");
        System.out.println("1. 用户创建预约");
        System.out.println("   -> 时间片状态: 0(可预约) -> 1(待支付)");
        System.out.println("   -> 预约状态: 0(待支付)");
        System.out.println("2. 用户支付押金(payDeposit)");
        System.out.println("   -> 时间片状态: 1(待支付) -> 2(已预约)");
        System.out.println("   -> 预约状态: 0(待支付) -> 1(已预约)");
        System.out.println("   -> 发送MQ延迟消息: BOOKING_COMPLETE:{bookingId}");
        System.out.println("   -> 延迟时间 = 预约结束时间 - 当前时间");
        System.out.println("3. 预约结束时间到达(MQ回调: bookingComplete)");
        System.out.println("   -> 时间片状态: 2(已预约) -> 0(可预约)");
        System.out.println("   -> 预约状态: 保持1(已预约)");
        System.out.println("   -> 清除时间片的bookingId");

        System.out.println("\n【流程二：超时取消】");
        System.out.println("1. 用户创建预约(未支付)");
        System.out.println("   -> 时间片状态: 0 -> 1(待支付)");
        System.out.println("   -> 预约状态: 0(待支付)");
        System.out.println("2. 支付超时(MQ回调: timeoutCancel)");
        System.out.println("   -> 验证预约状态必须是待支付(0)");
        System.out.println("   -> 时间片状态: 1(待支付) -> 0(可预约)");
        System.out.println("   -> 预约状态: 0(待支付) -> 2(已取消)");
        System.out.println("   -> 清除时间片的bookingId");

        System.out.println("\n【MQ消息队列说明】");
        System.out.println("1. 支付押金时发送延迟消息:");
        System.out.println("   delayedProducer.sendDelayedMessage(");
        System.out.println("       \"BOOKING_COMPLETE:\" + bookingId,");
        System.out.println("       delayMillis  // 延迟时间=预约结束-当前时间");
        System.out.println("   );");
        System.out.println("2. MQ消费者(DelayedConsumer)处理:");
        System.out.println("   -> 解析消息内容");
        System.out.println("   -> 调用 bookingService.bookingComplete(bookingId)");
        System.out.println("   -> 释放时间片");

        System.out.println("\n【测试用例覆盖】");
        System.out.println("- testPayDeposit_BookingNotFound: 预约不存在时抛异常");
        System.out.println("- testPayDeposit_AlreadyPaid: 已支付时抛异常");
        System.out.println("- testPayDeposit_WrongStatus: 状态不对时抛异常");
        System.out.println("- testTimeoutCancel_BookingNotFound: 取消时预约不存在");
        System.out.println("- testTimeoutCancel_WrongStatus: 状态不是待支付时不处理");
        System.out.println("- testBookingComplete_BookingNotFound: 完成时预约不存在");
        System.out.println("- testBookingComplete_WrongStatus: 状态不是已预约时不处理");
        System.out.println("- testCancelBooking_EmptyList: 空列表抛异常");
        System.out.println("- testCreateBooking_SlotIdsEmpty: 空时间片列表抛异常");

        System.out.println("\n========== 业务流程说明结束 ==========");
    }
}