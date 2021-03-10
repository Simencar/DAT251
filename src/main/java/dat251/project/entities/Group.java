package dat251.project.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String groupName;

    @JsonIgnore
    @ManyToMany
    private List<User> members;

    // Construct the object to be included in the JSON response instead of members
    @JsonProperty("members")
    public List<String> getMembersAsJsonString() {
        List<String> memberNames = new ArrayList<>();
        for (User member : members) {
            memberNames.add(member.getName());
        }
        return memberNames;
    }

    public Group() {

    }

    public Group(String groupName) {
        if (groupName.length() < 3) {
            throw new IllegalArgumentException("Group name must be at least three characters long");
        }
        this.groupName = groupName;
        this.members = new ArrayList<>();
    }

    public boolean addUserToGroup(User user) {
        if (!(user == null) && !members.contains(user)) {
            members.add(user);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeUserFromGroup(User user) {
        if (!(user == null) && members.contains(user)) {
            members.remove(user);
            return true;
        } else {
            return false;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return String.format(
                "Group[Id='%d', " +
                        "groupName='%s', " +
                        "members='%s']",
                id, groupName, printMembers()
        );
    }

    private String printMembers() {
        if (members.isEmpty()) {
            return "[]";
        }
        StringBuilder out = new StringBuilder("[ ");
        for (User user : members) {
            out.append(user.getUserName()).append(", ");
        }
        out.deleteCharAt(out.length() - 2);
        out.append("]");
        return out.toString();
    }
}
