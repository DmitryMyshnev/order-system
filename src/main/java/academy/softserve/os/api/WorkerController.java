package academy.softserve.os.api;

import academy.softserve.os.api.dto.WorkerDTO;
import academy.softserve.os.api.dto.command.CreateWorkerCommandDTO;
import academy.softserve.os.mapper.WorkerMapper;
import academy.softserve.os.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
public class WorkerController {
    private static final WorkerMapper WORKER_MAPPER = WorkerMapper.INSTANCE;
    private final WorkerService workerService;
    @Autowired
    WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("api/admin/worker")
    @Operation(summary = "Create a new Worker")
    public ResponseEntity<WorkerDTO> createWorker(@RequestBody @Valid CreateWorkerCommandDTO commandDTO) {
        var createWorkerCommand = WORKER_MAPPER.toCreateWorkerCommand(commandDTO);
        var worker = workerService.createWorker(createWorkerCommand);

        return ResponseEntity.status(HttpStatus.CREATED).body(WORKER_MAPPER.toWorkerDTO(worker));
    }
}
