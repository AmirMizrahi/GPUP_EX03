package DTO;

import java.util.List;

public class PathsBetweenTwoTargetsDTO {
    private List<String> list;

    public PathsBetweenTwoTargetsDTO(List<String> list) {
        this.list = list;
    }

    public List<String> getPathsBetweenTwoTargets(){return this.list;}
}
