package com.cocobongo.cerveceria.outgoings.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.outgoings.dto.BalanceReport;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingRequestDTO;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingResponseDTO;
import com.cocobongo.cerveceria.outgoings.entities.OutgoingEntity;
import com.cocobongo.cerveceria.outgoings.repositories.OutgoingsRepository;
import com.cocobongo.cerveceria.reports.services.ReportsService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutgoingsService {

          @Autowired
          private OutgoingsRepository repo;

          @Autowired
          private ReportsService reports;

          @Transactional
          public Page<OutgoingResponseDTO> findAll(Pageable pageable) {
                    return repo.findAll(pageable)
                                        .map(this::toResponseDTO);
          }

          @Transactional
          public OutgoingResponseDTO findById(Integer id) {
                    return repo.findById(id)
                                        .map(this::toResponseDTO)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                            "No se encontro un gasto con el id: " + id));
          }

          @Transactional
          public OutgoingResponseDTO create(OutgoingRequestDTO outgoing) {
                    if (outgoing.getDate() == null) {
                              throw new BusinessException("La fecha no puede ser null");
                    }
                    if (outgoing.getType() == null) {
                              throw new BusinessException("El tipo no puede ser null");
                    }
                    if (outgoing.getTotal() == null) {
                              throw new BusinessException("El total del gasto no puede ser null");
                    }

                    OutgoingEntity o = OutgoingEntity.builder()
                                        .idBranch(outgoing.getIdBranch())
                                        .idUser(outgoing.getIdUser())
                                        .type(outgoing.getType())
                                        .date(outgoing.getDate())
                                        .total(outgoing.getTotal())
                                        .description(outgoing.getDescription())
                                        .build();

                    repo.save(o);
                    return toResponseDTO(o);
          }

          @Transactional
          public OutgoingResponseDTO update(OutgoingRequestDTO upOutgoing, Integer id) {
                    OutgoingEntity up = repo.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                            "No se encontro un gasto con el id: " + id));
                    if (upOutgoing.getDate() == null) {
                              throw new BusinessException("La fecha no puede ser null");
                    }
                    if (upOutgoing.getType() == null) {
                              throw new BusinessException("El tipo no puede ser null");
                    }
                    if (upOutgoing.getTotal() == null) {
                              throw new BusinessException("El total del gasto no puede ser null");
                    }

                    up.setIdBranch(upOutgoing.getIdBranch());
                    up.setIdUser(upOutgoing.getIdUser());
                    up.setType(upOutgoing.getType());
                    up.setDate(upOutgoing.getDate());
                    up.setTotal(upOutgoing.getTotal());
                    up.setDescription(upOutgoing.getDescription());

                    repo.save(up);
                    return toResponseDTO(up);

          }

          public BalanceReport generarBalance(LocalDateTime inicio, LocalDateTime fin, Integer id) {

                    BigDecimal ingresos = reports.getSummary(inicio, fin, id).getEstimatedProfit();

                    BigDecimal gastos = repo.sumarGastos(inicio, fin, id);

                    BigDecimal balance = ingresos.subtract(gastos);

                    BalanceReport dto = BalanceReport.builder()
                                        .gastos(gastos)
                                        .balance(balance)
                                        .fechaInicio(inicio)
                                        .fechaFin(fin)
                                        .build();

                    return dto;
          }

          @Transactional
          public void deleteOutgoing(Integer id) {
                    repo.deleteById(id);
          }

          private OutgoingResponseDTO toResponseDTO(OutgoingEntity entity) {
                    if (entity == null) {
                              return null;
                    }

                    return OutgoingResponseDTO.builder()
                                        .idOutgoing(entity.getIdOutgoing())
                                        .idBranch(entity.getIdBranch())
                                        .idUser(entity.getIdUser())
                                        .type(entity.getType())
                                        .date(entity.getDate())
                                        .total(entity.getTotal())
                                        .description(entity.getDescription())
                                        .build();
          }
}
