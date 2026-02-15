package com.saiteja.portfolio_backend.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "pending_registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRegistration {
    @Id
    private String id;
    private String email;
    private String role;
    private String otpHash;
    private String passwordHash;
    private Date expiryTime;
}
