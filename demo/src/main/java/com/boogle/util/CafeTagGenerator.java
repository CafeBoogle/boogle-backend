package com.boogle.util;

import com.boogle.dto.CafeScoreResopnseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CafeTagGenerator {

    private static final double TAG_THRESHOLD = 3.5;

    public List<String> generateTags(CafeScoreResopnseDto dto) {
        // 리뷰가 없으면 태그도 없어야함
        if(dto == null || dto.getReviewCount() == 0) {
            return List.of();
        }

        List<String> tags = new ArrayList<>();

        if(isOver(dto.getToiletScoreAvg())){
            tags.add("깨끗한 화장실");
        }
        if(isOver(dto.getNoiseScoreAvg())){
            tags.add("조용한 분위기");
        }
        if(isOver(dto.getSeatScoreAvg())){
            tags.add("충분한 좌석");
        }
        if(isOver(dto.getOutletScoreAvg())){
            tags.add("충분한 콘센트");
        }
        if(isOver(dto.getWifiScoreAvg())){
            tags.add("빠른 와이파이");
        }
        if(isOver(dto.getStudyScoreAvg())){
            tags.add("카공 추천");
        }

        return tags;
    }

    private boolean isOver(Double score) {
        return score != null && score >= TAG_THRESHOLD;
    }

    // Null이면 0.0 반환
    private double nvl(Double val) {
        return (val == null) ? 0.0 : val;
    }
}
