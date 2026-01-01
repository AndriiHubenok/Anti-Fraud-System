package org.example.controller;

import org.example.model.SuspiciousIp;
import org.example.service.IpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
public class SuspiciousIpController {
    @Autowired
    IpService ipService;

    private record IpRequest(@NotBlank String ip) { }
    private record IpDeleteResponse(String status) { }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<SuspiciousIp>> getListOfSuspiciousIps() {
        return ResponseEntity.ok(ipService.getListOfSuspiciousIps());
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity<SuspiciousIp> addSuspiciousIp(@RequestBody @Valid SuspiciousIpController.IpRequest ipRequest) {
        if(!ipService.isValidIp(ipRequest.ip())){
            return ResponseEntity.badRequest().build();
        }
        SuspiciousIp ip = ipService.addSuspiciousIp(ipRequest.ip());
        return ip == null ? ResponseEntity.status(409).build() : ResponseEntity.ok(ip);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<?> deleteSuspiciousIp(@PathVariable String ip) {
        if (!ipService.isValidIp(ip)) {
            return ResponseEntity.badRequest().build();
        }

        return ipService.deleteSuspiciousIp(ip) ? ResponseEntity.ok(new IpDeleteResponse("IP " + ip + " successfully removed!")) : ResponseEntity.notFound().build();
    }
}
