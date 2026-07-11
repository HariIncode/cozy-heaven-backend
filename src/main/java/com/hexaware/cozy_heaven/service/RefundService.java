package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.RefundDTO;
import com.hexaware.cozy_heaven.dto.response.RefundResponseDTO;

public interface RefundService {

    RefundResponseDTO createRefund(RefundDTO refundDTO);

    RefundResponseDTO approveRefund(int refundId);

    RefundResponseDTO processRefund(int refundId);

    RefundResponseDTO rejectRefund(int refundId, String reason);

    RefundResponseDTO getRefundById(int refundId);

    RefundResponseDTO getRefundByBooking(int bookingId);

    List<RefundResponseDTO> getAllRefund();
}