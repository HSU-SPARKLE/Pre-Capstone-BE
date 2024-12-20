package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.service.ImageService;
import com.backend.sparkle.service.MessageService;
import com.backend.sparkle.service.PpurioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Tag(name = "문자 보내기 페이지", description = "문자 보내기 및 이미지 생성에 관한 API")
@RestController
@RequestMapping("/api/message")
public class MessageSendController {

    private final ImageService imageService;
    private final PpurioService ppurioService;
    private final MessageService messageService;

    @Autowired
    public MessageSendController(ImageService imageService, PpurioService ppurioService, MessageService messageService) {
        this.imageService = imageService;
        this.ppurioService = ppurioService;
        this.messageService = messageService;
    }

    @Operation(
            summary = "발송 목적 및 내용, 키워드 선택(분위기, 계절감), 키워드 입력 후 이미지 및 광고 문자 생성",
            description = "사용자가 발송 목적 및 내용, 키워드 선택(분위기, 계절감), 키워드 입력 후 이미지 생성하기 버튼을 클릭하여 3개의 이미지와 광고 문자 생성",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1")
            }
    )
    @PostMapping("/generate/{userId}")
    public ResponseEntity<CommonResponse<MessageDto.GeneratedImageMessageResponseDto>> createImages(@PathVariable("userId") Long userId, @RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
        log.info("이미지 생성 요청 userId: {}", userId);
        try {
            MessageDto.GeneratedImageMessageResponseDto responseDto = imageService.generateImages(requestDto);
            return ResponseEntity.ok(CommonResponse.success("이미지 생성 성공", responseDto));
        } catch (WebClientResponseException e) {
            log.error("Azure Dalle 이미지 생성 요청 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(CommonResponse.fail(e.getMessage()));
        }  catch (Exception e) {
            log.error("이미지 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.fail(e.getMessage()));
        }
    }

    @Operation(
            summary = "광고 메시지 생성",
            description = "발송 목적 및 내용을 기반으로 광고 메시지 생성",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1")
            }
    )
    @PostMapping("/generateMessage/{userId}")
    public ResponseEntity<CommonResponse<String>> generateAdvertiseMessage(
            @PathVariable("userId") Long userId,
            @RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
        log.info("광고 메시지 생성 요청 userId: {}", userId);
        try {
            // 광고 메시지 생성
            String advertiseMessage = imageService.generateAdvertiseMessage(requestDto.getInputMessage());
            return ResponseEntity.ok(CommonResponse.success("광고 메시지 생성 성공", advertiseMessage));
        } catch (Exception e) {
            log.error("광고 메시지 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.fail("광고 메시지 생성 실패"));
        }
    }

    @Operation(
            summary = "단일 전화번호와 주소록 엑셀 파일을 통해 문자 전송",
            description = "단일 전화번호와 업로드된 주소록에 있는 모든 전화번호로 문자 메시지를 전송합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1"),
            }
    )
    @PostMapping(value = "/send/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<CommonResponse<Long>> sendUnifiedMessage(
            @PathVariable("userId") Long userId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("requestDto") String requestDtoJson) {

        try {
            // JSON String을 SendRequestDto 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            MessageDto.SendRequestDto requestDto = objectMapper.readValue(requestDtoJson, MessageDto.SendRequestDto.class);

            log.info("sendMessage: {}", requestDto.getSendMessage());
            log.info("completeImageURL: {}", requestDto.getCompleteImageURL());

            // 수신 전화번호 목록에 단일 전화번호 추가
            List<String> recipientPhoneNumbers = new ArrayList<>();
            if (requestDto.getTestSendPhoneNumber() != null) {
                recipientPhoneNumbers.add(requestDto.getTestSendPhoneNumber());
            }

            // 엑셀 파일에서 전화번호 목록 추출
            recipientPhoneNumbers.addAll(extractPhoneNumbersFromExcel(file));

            // Ppurio API로 문자 전송 요청
            boolean sendSuccess = ppurioService.sendSmsWithImage(
                    requestDto.getSendPhoneNumber(),
                    recipientPhoneNumbers,
                    requestDto.getSendMessage(),
                    requestDto.getCompleteImageURL(),
                    requestDto.getSendType(),
                    requestDto.getSendDateTime()
            );

            if (sendSuccess) {
                messageService.addMessage(userId, requestDto); // 메시지 내역 저장
                return ResponseEntity.ok(CommonResponse.success("통합 문자 전송 성공", userId));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(CommonResponse.fail("통합 문자 전송 실패"));
            }
        } catch (Exception e) {
            log.error("통합 문자 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.fail("통합 문자 전송 실패"));
        }
    }

    private List<String> extractPhoneNumbersFromExcel(MultipartFile file) {
        List<String> phoneNumbers = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 선택
            for (Row row : sheet) {
                if (row.getCell(0) != null) {
                    String phoneNumber = row.getCell(0).getStringCellValue().trim();
                    phoneNumbers.add(phoneNumber);
                }
            }
        } catch (IOException e) {
            log.error("엑셀 파일에서 전화번호 추출 실패: {}", e.getMessage());
            throw new RuntimeException("엑셀 파일 처리 중 오류 발생", e);
        }
        return phoneNumbers;
    }

    // 문자 발송 완료 후 화면
    @Operation(
            summary = "문자 보내기 완료 후",
            description = "문자 보내기 완료 후 사용자가 보낸 메시지에 대한 정보를 화면에 띄운다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1")
            }
    )
    @GetMapping("/complete/{userId}")
    public ResponseEntity< CommonResponse<MessageDto.SendCompleteResponseDto> > getSendCompleteResponse (@PathVariable("userId") Long userId){
        try {
            return ResponseEntity.ok(CommonResponse.success("문자 보내기 완료 후 요청 성공", messageService.getSendCompleteMessage(userId)));
        } catch (NoSuchElementException e){
            log.error("문자 보내기 완료 후 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.fail("문자 보내기 완료 후 요청 실패"));
        }
    }

    // 테스트 발송 메서드 추가
    @Operation(
            summary = "이미지 + 텍스트 문자 테스트 발송",
            description = "템플릿 기능을 통해 완성된 이미지 + 텍스트 문자 테스트 발송 요청",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1")
            }
    )
    @PostMapping("/test/{userId}")
    public ResponseEntity<CommonResponse<Long>> sendTestMessage(@PathVariable("userId") Long userId, @RequestBody MessageDto.SendRequestDto requestDto) {
        try {
            log.info("테스트 문자 발송 요청 userId: {}", userId);

            // Ppurio API로 테스트 문자 전송
            boolean sendSuccess = ppurioService.sendSmsWithImage(
                    requestDto.getSendPhoneNumber(),
                    List.of(requestDto.getTestSendPhoneNumber()),
                    requestDto.getSendMessage(),
                    requestDto.getCompleteImageURL(),
                    requestDto.getSendType(),
                    requestDto.getSendDateTime()
            );

            if (sendSuccess) {
                return ResponseEntity.ok(CommonResponse.success("테스트 문자 발송 요청 성공", userId));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(CommonResponse.fail("테스트 문자 발송 요청 실패"));
            }
        } catch (Exception e) {
            log.error("테스트 문자 발송 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.fail("테스트 문자 발송 요청 실패"));
        }
    }

}
