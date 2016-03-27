package field;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class GroupData {
    List<List<Group>> groupTable;
    List<Group> uniqueGroups;

    public List<Group> getUniqueGroups() {
        if ( this.uniqueGroups != null ) {
            return this.uniqueGroups;
        }
        this.uniqueGroups = this.groupTable.stream().flatMap( g -> g.stream() ).filter( g -> g != null ).distinct().collect( Collectors
                .<Group> toList() );
        return this.uniqueGroups;
    }

    public GroupData( List<List<Group>> table ) {
        this.groupTable = table;
    }
}
