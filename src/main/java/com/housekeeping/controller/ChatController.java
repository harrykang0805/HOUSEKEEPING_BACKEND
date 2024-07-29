package com.housekeeping.controller;

import com.housekeeping.DTO.ChatRoomDTO;
import com.housekeeping.DTO.MessageDTO;
import com.housekeeping.entity.ChatRoom;
import com.housekeeping.entity.Message;
import com.housekeeping.service.ChatService;
import com.housekeeping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // 채팅 방 생성
    @PostMapping("/room/create")
    public ChatRoomDTO createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO) {

        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(chatRoomDTO.getChatRoomName())
                .chatRoomType(chatRoomDTO.getChatRoomType())
                .chatRoomUpdatedAt(LocalDateTime.now())
                .build();

        ChatRoom result = chatService.saveChatRoom(chatRoom);

        for (Long userId : chatRoomDTO.getUserIdList()) {
            chatService.inviteUser(result.getChatRoomId(), userId);
        }

        return ChatRoomDTO.builder()
                .chatRoomId(result.getChatRoomId())
                .build();
    }

    // 채팅 방 리스트 반환
    @GetMapping("/room/list")
    public List<ChatRoomDTO> getChatRooms(@RequestParam("userId") Long userId) {

        return chatService.getChatRoomsByUserId(userId);
    }

    // 채팅 방 나감
    @DeleteMapping("/room/quit")
    public ResponseEntity<String> quitChatRoom(@RequestParam("chatRoomId") Long chatRoomId, @RequestParam("userId") Long userId) {

        chatService.quitChatRoom(chatRoomId, userId);
        return ResponseEntity.ok().build();
    }

    // 메시지를 DB에 저장
    @PostMapping("/message/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {

        Message message = new Message();

        message.setChatRoom(chatService.getChatRoomById(messageDTO.getChatRoomId()));
        message.setMessageSender(userService.getUserById(messageDTO.getMessageSenderId()));
        message.setMessageContent(messageDTO.getMessageContent());

        ChatRoom chatRoom = message.getChatRoom();
        chatRoom.setChatRoomUpdatedAt(LocalDateTime.now());

        chatService.saveChatRoom(chatRoom);
        chatService.saveMessage(message);

        return ResponseEntity.ok().build();
    }

    // 채팅 방의 메시지 목록을 받아옴
    @GetMapping("/message/list")
    public List<MessageDTO> getMessages(@RequestParam("chatRoomId") Long chatRoomId) {

        List<Message> messageList = chatService.getMessagesByChatRoomId(chatRoomId);
        List<MessageDTO> messageDTOList = new ArrayList<>();

        for (Message message : messageList) {
            messageDTOList.add(
                    MessageDTO.builder()
                            .messageId(message.getMessageId())
                            .messageSenderId(message.getMessageSender().getUserId())
                            .messageSenderNickname(message.getMessageSender().getNickname())
                            .chatRoomId(message.getChatRoom().getChatRoomId())
                            .messageContent(message.getMessageContent())
                            .messageTimestamp(message.getMessageTimestamp())
                            .build()
            );
        }

        return messageDTOList;
    }

    // 특정 메시지를 특정 유저가 읽음으로 처리
    @PutMapping("/message/read")
    public ResponseEntity<String> readMessage(@RequestParam("messageId") Long messageId, @RequestParam("userId") Long userId) {

        chatService.markMessageAsRead(messageId, userId);

        return ResponseEntity.ok().build();
    }

    // 특정 방의 모든 메시지를 특정 유저가 읽음으로 처리
    @PutMapping("/message/read/all")
    public ResponseEntity<String> readAllMessages(@RequestParam("roomId") Long roomId, @RequestParam("userId") Long userId) {

        List<Long> readStatusIds = chatService.getUnreadMessageIds(roomId, userId);

        for (Long readStatusId : readStatusIds) {
            chatService.updateReadStatusTrue(readStatusId);
        }

        return ResponseEntity.ok().build();
    }
}
