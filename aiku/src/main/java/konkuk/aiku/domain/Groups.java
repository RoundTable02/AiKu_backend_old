package konkuk.aiku.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Groups extends TimeEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "groupId")
    private Long id;

    private String groupName;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserGroup> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    private String description;

    private Groups(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    //==생성 메서드==
    public static Groups createGroups(Users user, String groupName, String description){
        Groups group = new Groups(groupName, description);
        group.addUser(user);
        return group;
    }

    //==편의 메서드==
    public void modifyGroup(String groupName, String description){
        this.groupName = groupName;
        this.description = description;
    }

    public void addUser(Users user){
        UserGroup userGroup = new UserGroup(user, this);
        this.userGroups.add(userGroup);
    }

    public void addSchedule(Schedule schedule){
        schedules.add(schedule);
    }
}
