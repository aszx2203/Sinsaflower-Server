package com.sinsaflower.server.domain.delivery.service;

import com.sinsaflower.server.domain.delivery.entity.Region;
import com.sinsaflower.server.domain.delivery.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
// @RequiredArgsConstructor
@Slf4j
public class DeliveryDataInitializer {
// implements CommandLineRunner {

    // private final RegionRepository regionRepository;
    // private final ProductCategoryRepository productCategoryRepository;

    // @Override
    // public void run(String... args) throws Exception {
    //     initializeRegions();
    //     initializeProductCategories();
    // }

    // private void initializeRegions() {
    //     log.info("지역 데이터 초기화 시작");
        
    //     List<RegionData> regions = Arrays.asList(
    //         // 강원도도
    //         new RegionData("강원도", "춘천시", "200"),
    //         new RegionData("강원도", "화천군", "201"),
    //         new RegionData("강원도", "원주시", "202"),
    //         new RegionData("강원도", "홍천군", "203"),
    //         new RegionData("강원도", "횡성군", "204"),
    //         new RegionData("강원도", "평창군", "205"),
    //         new RegionData("강원도", "정선군", "206"),
    //         new RegionData("강원도", "철원군", "207"),
    //         new RegionData("강원도", "화천군", "208"),
    //         new RegionData("강원도", "양구군", "209"),
    //         new RegionData("강원도", "인제군", "210"),
    //         new RegionData("강원도", "고성군", "211"),
    //         new RegionData("강원도", "속초시", "212"),
    //         new RegionData("강원도", "동해시", "213"),
    //         new RegionData("강원도", "삼척시", "214"),
    //         new RegionData("강원도", "태백시", "215"),
    //         new RegionData("강원도", "영월군", "216"),
    //         new RegionData("강원도", "양양군", "217"),
            
    //         // 경기도도 (일부)
    //         new RegionData("경기도", "수원시", "100"),
    //         new RegionData("경기도", "성남시", "101"),
    //         new RegionData("경기도", "용인시", "102"),
    //         new RegionData("경기도", "안양시", "103"),
    //         new RegionData("경기도", "안산시", "104"),
    //         new RegionData("경기도", "과천시", "105"),
    //         new RegionData("경기도", "광명시", "106"),
    //         new RegionData("경기도", "부천시", "107"),
    //         new RegionData("경기도", "시흥시", "108"),
    //         new RegionData("경기도", "김포시", "109"),
    //         new RegionData("경기도", "광주시", "110"),
    //         new RegionData("경기도", "양주시", "111"),
    //         new RegionData("경기도", "의정부시", "112"),
    //         new RegionData("경기도", "동두천시", "113"),
    //         new RegionData("경기도", "구리시", "114"),
    //         new RegionData("경기도", "남양주시", "115"),
    //         new RegionData("경기도", "오산시", "116"),
    //         new RegionData("경기도", "화성시", "117"),
    //         new RegionData("경기도", "평택시", "118"),
    //         new RegionData("경기도", "이천시", "119"),
    //         new RegionData("경기도", "안성시", "120"),
    //         new RegionData("경기도", "김포시", "121"),
    //         new RegionData("경기도", "여주시", "122"),
    //         new RegionData("경기도", "양평군", "123"),
    //         new RegionData("경기도", "가평군", "124"),
    //         new RegionData("경기도", "연천군", "125"),
            
    //         // 서울특별시특별시 (일부)
    //         new RegionData("서울특별시", "종로구", "010"),
    //         new RegionData("서울특별시", "중구", "011"),
    //         new RegionData("서울특별시", "용산구", "012"),
    //         new RegionData("서울특별시", "성동구", "013"),
    //         new RegionData("서울특별시", "광진구", "014"),
    //         new RegionData("서울특별시", "동대문구", "015"),
    //         new RegionData("서울특별시", "중랑구", "016"),
    //         new RegionData("서울특별시", "성북구", "017"),
    //         new RegionData("서울특별시", "강북구", "018"),
    //         new RegionData("서울특별시", "도봉구", "019"),
    //         new RegionData("서울특별시", "노원구", "020"),
    //         new RegionData("서울특별시", "은평구", "021"),
    //         new RegionData("서울특별시", "서대문구", "022"),
    //         new RegionData("서울특별시", "마포구", "023"),
    //         new RegionData("서울특별시", "양천구", "024"),
    //         new RegionData("서울특별시", "강서구", "025"),
    //         new RegionData("서울특별시", "구로구", "026"),
    //         new RegionData("서울특별시", "금천구", "027"),
    //         new RegionData("서울특별시", "영등포구", "028"),
    //         new RegionData("서울특별시", "동작구", "029"),
    //         new RegionData("서울특별시", "관악구", "030"),
    //         new RegionData("서울특별시", "서초구", "031"),
    //         new RegionData("서울특별시", "강남구", "032"),
    //         new RegionData("서울특별시", "송파구", "033"),
    //         new RegionData("서울특별시", "강동구", "034")
    //     );
        
    //     for (RegionData regionData : regions) {
    //         if (!regionRepository.existsBySidoAndSigungu(regionData.sido, regionData.sigungu)) {
    //             Region region = Region.builder()
    //                 .sido(regionData.sido)
    //                 .sigungu(regionData.sigungu)
    //                 .zipcode(regionData.zipcode)
    //                 .isActive(true)
    //                 .build();
    //             regionRepository.save(region);
    //             log.debug("지역 생성: {}", region.getFullName());
    //         }
    //     }
        
    //     log.info("지역 데이터 초기화 완료");
    // }

    // private void initializeProductCategories() {
    //     log.info("상품 카테고리 데이터 초기화 시작");
        
    //     List<CategoryData> categories = Arrays.asList(
    //         new CategoryData("축하", "축하", "축하용 화환", 1),
    //         new CategoryData("근조", "근조", "근조용 화환", 2),
    //         new CategoryData("오목체", "오목체", "오목체 화환", 3),
    //         new CategoryData("동양", "동양란", "동양란 화환", 4),
    //         new CategoryData("서양", "서양란", "서양란 화환", 5),
    //         new CategoryData("꽃", "꽃", "일반 꽃 화환", 6),
    //         new CategoryData("관엽", "관엽식물", "관엽식물 화환", 7),
    //         new CategoryData("쌀", "쌀", "쌀 화환", 8),
    //         new CategoryData("기타", "기타", "기타 화환", 9),
    //         new CategoryData("과일", "과일", "과일 화환", 10)
    //     );
        
    //     for (CategoryData categoryData : categories) {
    //         if (!productCategoryRepository.existsByName(categoryData.name)) {
    //             ProductCategory category = ProductCategory.builder()
    //                 .name(categoryData.name)
    //                 .displayName(categoryData.displayName)
    //                 .description(categoryData.description)
    //                 .sortOrder(categoryData.sortOrder)
    //                 .isActive(true)
    //                 .isDeliverable(true)
    //                 .build();
    //             productCategoryRepository.save(category);
    //             log.debug("상품 카테고리 생성: {}", category.getName());
    //         }
    //     }
        
    //     log.info("상품 카테고리 데이터 초기화 완료");
    // }

    // private static class RegionData {
    //     final String sido;
    //     final String sigungu;
    //     final String zipcode;

    //     RegionData(String sido, String sigungu, String zipcode) {
    //         this.sido = sido;
    //         this.sigungu = sigungu;
    //         this.zipcode = zipcode;
    //     }
    // }

    // private static class CategoryData {
    //     final String name;
    //     final String displayName;
    //     final String description;
    //     final int sortOrder;

    //     CategoryData(String name, String displayName, String description, int sortOrder) {
    //         this.name = name;
    //         this.displayName = displayName;
    //         this.description = description;
    //         this.sortOrder = sortOrder;
    //     }
    // }
} 