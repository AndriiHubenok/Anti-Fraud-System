package org.example.service;

import org.example.model.SuspiciousIp;
import org.example.repository.SuspiciousIpRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IpService {
    @Autowired
    private SuspiciousIpRepository ipRepository;

    public List<SuspiciousIp> getListOfSuspiciousIps() {
        return ipRepository.findAllByOrderByIdAsc();
    }

    public SuspiciousIp getSuspiciousIp(String ip) {
        return ipRepository.findByIp(ip).orElse(null);
    }

    public SuspiciousIp addSuspiciousIp(String ip) {
        try{
            return ipRepository.save(new SuspiciousIp(ip));
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public boolean deleteSuspiciousIp(String ip) {
        SuspiciousIp entity = ipRepository.findByIp(ip).orElse(null);
        if (entity == null) {
            return false;
        }
        ipRepository.delete(entity);
        return true;
    }

    public boolean isValidIp(String ip) {
        String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
}
