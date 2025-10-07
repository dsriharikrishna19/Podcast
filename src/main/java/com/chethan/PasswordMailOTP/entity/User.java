package com.chethan.PasswordMailOTP.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Entity
@Setter
@Getter
@AllArgsConstructor
@Builder
@Data
@Table(name="users")
public class User {

    public User() {}
    public User(String email, String password, String name, String gender, Long phoneNumber) {
        this.email = email;
        this.password = password;
        this.name=name;
        this.gender=gender;
        this.phoneNumber=phoneNumber;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    private String name;
    private String gender;
    private Long phoneNumber;
    @Column(nullable = false)
    private boolean isPremium=false;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
