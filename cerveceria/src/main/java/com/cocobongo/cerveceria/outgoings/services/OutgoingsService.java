package com.cocobongo.cerveceria.outgoings.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.common.utils.CurrencyConverter;
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import com.cocobongo.cerveceria.exchangerate.services.ExchangeRateService;
import com.cocobongo.cerveceria.outgoings.dto.BalanceReport;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingRequestDTO;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingResponseDTO;
import com.cocobongo.cerveceria.outgoings.entities.OutgoingEntity;
import com.cocobongo.cerveceria.outgoings.repositories.OutgoingsRepository;
import com.cocobongo.cerveceria.reports.services.ReportsService;
import com.cocobongo.cerveceria.sales.entities.Currency;
import com.cocobongo.cerveceria.users.entities.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutgoingsService {

    private final OutgoingsRepository repo;
    private final ReportsService reports;
    private final ExchangeRateService exchangeRateService;

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
    public OutgoingResponseDTO create(OutgoingRequestDTO outgoing, UserEntity currentUser) {
        // Validaciones
        if (outgoing.getType() == null) {
            throw new BusinessException("El tipo no puede ser null");
        }
        if (outgoing.getTotal() == null) {
            throw new BusinessException("El total del gasto no puede ser null");
        }

        // Determinar la moneda (por defecto USD)
        Currency currency = outgoing.getCurrency() != null ? outgoing.getCurrency() : Currency.USD;

        // Construir la entidad base
        OutgoingEntity.OutgoingEntityBuilder builder = OutgoingEntity.builder()
                .idBranch(BranchEntity.builder().idBranch(outgoing.getIdBranch()).build())
                .idUser(currentUser) // Usuario autenticado recibido como parámetro
                .type(outgoing.getType())
                .date(resolveDate(outgoing.getDate()))
                .total(outgoing.getTotal())
                .description(outgoing.getDescription())
                .currency(currency);

        // Si la moneda es VES, asociar la tasa de cambio actual para trazabilidad
        if (currency == Currency.VES) {
            ExchangeRateEntity currentRate = exchangeRateService.getCurrentEntity();
            builder.rate(currentRate);
            log.debug("Gasto en VES registrado con tasa de cambio ID: {}", currentRate.getIdRate());
        }

        OutgoingEntity o = builder.build();
        repo.save(o);
        
        return toResponseDTO(o);
    }

    @Transactional
    public OutgoingResponseDTO update(OutgoingRequestDTO upOutgoing, Integer id, UserEntity currentUser) {
        OutgoingEntity up = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro un gasto con el id: " + id));
        
        // Validaciones
        if (upOutgoing.getType() == null) {
            throw new BusinessException("El tipo no puede ser null");
        }
        if (upOutgoing.getTotal() == null) {
            throw new BusinessException("El total del gasto no puede ser null");
        }

        // Actualizar campos básicos
        up.setIdBranch(BranchEntity.builder().idBranch(upOutgoing.getIdBranch()).build());
        up.setIdUser(currentUser); // Usuario autenticado recibido como parámetro
        up.setType(upOutgoing.getType());
        up.setDate(resolveDate(upOutgoing.getDate()));
        up.setTotal(upOutgoing.getTotal());
        up.setDescription(upOutgoing.getDescription());

        // Manejar cambio de moneda
        Currency currency = upOutgoing.getCurrency() != null ? upOutgoing.getCurrency() : Currency.USD;
        up.setCurrency(currency);
        
        // Si es VES, asociar la tasa actual; si no, eliminar la referencia
        if (currency == Currency.VES) {
            ExchangeRateEntity currentRate = exchangeRateService.getCurrentEntity();
            up.setRate(currentRate);
            log.debug("Gasto actualizado a VES con nueva tasa de cambio ID: {}", currentRate.getIdRate());
        } else {
            up.setRate(null); // USD no necesita tasa
        }

        repo.save(up);
        return toResponseDTO(up);
    }

    public BalanceReport generarBalance(LocalDateTime inicio, LocalDateTime fin, Integer id) {
        BigDecimal ingresos = reports.getSummary(inicio, fin, id).getEstimatedProfit();
        BigDecimal gastos = repo.sumarGastos(inicio, fin, id);
        BigDecimal balance = ingresos.subtract(gastos);

        return BalanceReport.builder()
                .gastos(gastos)
                .balance(balance)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .build();
    }

    @Transactional
    public void deleteOutgoing(Integer id) {
        repo.deleteById(id);
    }

    private OutgoingResponseDTO toResponseDTO(OutgoingEntity entity) {
        if (entity == null) {
            return null;
        }

        // Obtener la tasa de cambio según el contexto
        BigDecimal exchangeRate;
        
        // Si el gasto es en VES, usar la tasa histórica guardada
        if (entity.getCurrency() == Currency.VES && entity.getRate() != null) {
            exchangeRate = entity.getRate().getRate();
        } else {
            // Para gastos en USD o si no hay tasa histórica, usar la tasa actual
            exchangeRate = exchangeRateService.getCurrentRate();
        }

        // Calcular equivalentes según la moneda original
        BigDecimal totalUsd;
        BigDecimal totalVes;
        
        if (entity.getCurrency() == Currency.VES) {
            // El gasto se hizo en VES: total es en VES, convertir a USD
            totalVes = entity.getTotal();
            totalUsd = CurrencyConverter.vesToUsd(entity.getTotal(), exchangeRate);
        } else {
            // El gasto se hizo en USD: total es en USD, convertir a VES
            totalUsd = entity.getTotal();
            totalVes = CurrencyConverter.usdToVes(entity.getTotal(), exchangeRate);
        }

        return OutgoingResponseDTO.builder()
                .idOutgoing(entity.getIdOutgoing())
                .idBranch(entity.getIdBranch() != null ? entity.getIdBranch().getIdBranch() : null)
                .idUser(entity.getIdUser() != null ? entity.getIdUser().getIdUser() : null)
                .type(entity.getType())
                .date(entity.getDate())
                .description(entity.getDescription())
                .currency(entity.getCurrency())
                .total(entity.getTotal())           // Monto en moneda original
                .totalUsd(totalUsd)                 // Equivalente en USD
                .totalVes(totalVes)                 // Equivalente en VES
                .exchangeRate(exchangeRate)         // Tasa usada para la conversión
                .build();
    }

    private LocalDateTime resolveDate(LocalDateTime date) {
        return date != null ? date : LocalDateTime.now();
    }
}