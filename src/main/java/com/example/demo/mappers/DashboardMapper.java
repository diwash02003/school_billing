package com.example.demo.mappers;

import com.example.demo.dtos.DashboardSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author diwash
 * @created 12/28/25
 */

@Mapper
public interface DashboardMapper {
    DashboardSummary getDashboardSummary();
}
