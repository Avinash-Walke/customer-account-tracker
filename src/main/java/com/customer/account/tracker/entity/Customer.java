package com.customer.account.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;

    private String lastName;


    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String mobileNumber;

    private String address;

    private LocalDate dob;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        if (Objects.isNull(account)) return;

        if (!this.accounts.contains(account)) {
            this.accounts.add(account);
        }
        account.setCustomer(this);
    }

    public void removeAccount(Account account) {
        if (Objects.isNull(account)) return;

        if (this.accounts.remove(account)) {
            account.setCustomer(null);
        }
    }
}
