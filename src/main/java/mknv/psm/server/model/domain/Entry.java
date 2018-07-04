package mknv.psm.server.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author mknv
 */
@Entity
@Table(name = "entries")
public class Entry implements Serializable {

    private static final long serialVersionUID = -4909528805600683351L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entries_seq")
    @SequenceGenerator(name = "entries_seq", sequenceName = "entries_seq", allocationSize = 1)
    @Column
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column
    private String name;

    @Size(max = 255)
    @Column
    private String login;

    @Size(max = 255)
    @Column
    private String email;

    @Column
    private String password;

    @Size(max = 1000)
    @Column
    private String description;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @JsonIgnore
    @NotNull
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Group group;

    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    public Entry() {
    }

    public Entry(String name, String login, String email, String password, String description, LocalDate expiredDate, Group group, User user) {
        this.name = name;
        this.login = login;
        this.email = email;
        this.password = password;
        this.description = description;
        this.expiredDate = expiredDate;
        this.group = group;
        this.user = user;
    }

    public Entry(Integer id, String name, String login, String email, String password, String description, LocalDate expiredDate, Group group, User user) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.email = email;
        this.password = password;
        this.description = description;
        this.expiredDate = expiredDate;
        this.group = group;
        this.user = user;
    }

    @Transient
    public Integer getDaysLeft() {
        if (expiredDate == null) {
            return null;
        }
        LocalDate now = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(now, expiredDate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entry other = (Entry) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entry{" + "id=" + id + ", name=" + name + ", login=" + login + ", email=" + email + ", password=" + password + ", description=" + description + ", expiredDate=" + expiredDate + '}';
    }

}
