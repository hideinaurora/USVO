package org.example.service.venue;

import org.example.dto.admin.CourtAddDTO;
import org.example.dto.admin.CourtUpdateDTO;
import org.example.dto.admin.VenueAddDTO;
import org.example.dto.admin.VenueUpdateDTO;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.vo.admin.BookingListItemVO;
import org.example.vo.admin.CourtListItemVO;
import org.example.vo.admin.VenueListItemVO;
import org.example.vo.venue.VenueDetailVO;
import org.example.vo.venue.VenueListVO;
import org.example.common.PageResult;

import java.util.List;
import java.util.Map;

public interface VenueService {

    List<VenueListVO> listVenues(String type, String keyword, Double latitude, Double longitude);

    PageResult<VenueListVO> pageVenues(Integer pageNum, Integer pageSize, String type, String keyword, Double latitude, Double longitude);

    VenueDetailVO getVenueDetail(Long venueId);

    Map<String, Object> getCourtPageList(Long venueId, String type, Integer status, Integer page, Integer size);

    Long addCourt(CourtAddDTO dto);

    void updateCourt(Long courtId, CourtUpdateDTO dto);

    void deleteCourt(Long courtId);

    List<VenueListItemVO> getVenueList();

    Long addVenue(VenueAddDTO dto);

    void updateVenue(Long venueId, VenueUpdateDTO dto);

    void deleteVenue(Long venueId);

    Map<String, Object> getBookingPageList(Integer status, String username, String courtName, String startDate, String endDate, Integer page, Integer size);

    void deleteBooking(Long bookingId);

    void forceCancelBooking(Long bookingId, String reason);
}