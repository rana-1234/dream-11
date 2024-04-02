package main;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamFormat {
    final int requiredCount;
    final int keeperCount;
    final int batterCount;
    final int rounderCount;
    final int bowlerCount;
}
