package com.fileupload.fileproject.controller;


import com.fileupload.fileproject.requestDto.FileShareRequestDto;
import com.fileupload.fileproject.service.FileShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class FileShareController {

    private final FileShareService fileShareService;


    FileShareController(FileShareService fileShareService){
        this.fileShareService = fileShareService;
    }


    private String getClientIp(HttpServletRequest request)
    {

        String xfheader = request.getHeader("X-Forwarded-For");

        if(xfheader == null)
        {
            return request.getRemoteAddr();
        }

        return xfheader.split(",")[0];
    }

    @PostMapping("/shares")
    public ResponseEntity<?> shareFile(@RequestBody FileShareRequestDto dto,
                                       HttpServletRequest request)
    {

        String ip = getClientIp(request);
        fileShareService.shareFile(dto,ip);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
