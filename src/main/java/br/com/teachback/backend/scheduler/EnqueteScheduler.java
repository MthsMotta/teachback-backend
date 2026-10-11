package br.com.teachback.backend.scheduler;

import br.com.teachback.backend.service.EnqueteService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnqueteScheduler {

    private final EnqueteService enqueteService;

    @Scheduled(cron = "${teachback.enquete.cron-encerramento:0 0 6 21 * *}", zone = "America/Sao_Paulo")
    public void encerrarEnquetesVencidas() {
        enqueteService.encerrarEnquetesVencidas();
    }

    @Scheduled(cron = "${teachback.enquete.cron-geracao:0 0 6,22 28 * *}", zone = "America/Sao_Paulo")
    public void gerarEnquetesMensais() {
        enqueteService.gerarEnquetesMensais();
    }
}
