package konkuk.aiku.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AnalyticsBettingRatingServiceDto {

    List<AnalyticsBettingServiceDto> data;
}
