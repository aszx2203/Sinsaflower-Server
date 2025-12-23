package com.sinsaflower.server.domain.member.constants;

/**
 * 회원 관련 상수 클래스
 * 회원 상태, 메시지, 파일 업로드 등의 상수를 관리합니다.
 */
public final class MemberConstants {

    private MemberConstants() {
        // Prevent instantiation
    }

    /**
     * 회원 상태 관련 상수
     */
    public static class Status {
        public static final String PENDING = "PENDING";
        public static final String ACTIVE = "ACTIVE";
        public static final String SUSPENDED = "SUSPENDED";
        public static final String DELETED = "DELETED";
    }

    /**
     * 파일 업로드 관련 상수
     */
    public static class FileUpload {
        public static final String BUSINESS_CERT_PATH = "members/business-certs";
        public static final String BANK_CERT_PATH = "members/bank-certs";
        public static final String PROFILE_IMAGE_PATH = "members/profiles";
        
        // 허용 파일 확장자
        public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};
        public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf", "jpg", "jpeg", "png"};
        
        // 파일 크기 제한 (바이트)
        public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
        public static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB
    }

    /**
     * 회원 검증 관련 상수
     */
    public static class Validation {
        public static final int MIN_LOGIN_ID_LENGTH = 4;
        public static final int MAX_LOGIN_ID_LENGTH = 20;
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 20;
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 50;
        public static final int MOBILE_LENGTH = 11; // 01012345678 형태
    }

    /**
     * 회원 메시지 상수
     */
    public static class Messages {
        // 성공 메시지
        public static final String SIGNUP_SUCCESS = "회원가입이 성공적으로 완료되었습니다.";
        public static final String MEMBER_INFO_RETRIEVED = "회원 정보 조회가 성공적으로 완료되었습니다.";
        public static final String MEMBER_LIST_RETRIEVED = "회원 목록 조회가 성공적으로 완료되었습니다.";
        public static final String MEMBER_UPDATED = "회원 정보가 수정되었습니다.";
        public static final String MEMBER_STATUS_UPDATED = "회원 상태가 변경되었습니다.";
        public static final String MEMBER_DELETED = "회원이 삭제되었습니다.";
        public static final String PASSWORD_UPDATED = "비밀번호가 변경되었습니다.";
        public static final String PROFILE_IMAGE_UPLOADED = "프로필 이미지가 업로드되었습니다.";
        public static final String DELIVERY_REGION_UPLOADED = "배송설정이 저장되었습니다.";

        // 오류 메시지
        public static final String MEMBER_NOT_FOUND = "회원을 찾을 수 없습니다.";
        public static final String DUPLICATE_LOGIN_ID = "이미 사용 중인 로그인 ID입니다.";
        public static final String DUPLICATE_MOBILE = "이미 등록된 휴대전화번호입니다.";
        public static final String INVALID_PASSWORD = "현재 비밀번호가 일치하지 않습니다.";
        public static final String INVALID_STATUS_TRANSITION = "유효하지 않은 상태 변경입니다.";
        public static final String INACTIVE_MEMBER = "비활성화된 회원입니다.";
        public static final String PENDING_APPROVAL = "승인 대기 중인 회원입니다.";
        public static final String SUSPENDED_MEMBER = "정지된 회원입니다.";
        
        // 검증 메시지
        public static final String INVALID_LOGIN_ID_LENGTH = "로그인 ID는 4-20자 사이여야 합니다.";
        public static final String INVALID_PASSWORD_LENGTH = "비밀번호는 8-20자 사이여야 합니다.";
        public static final String INVALID_NAME_LENGTH = "이름은 2-50자 사이여야 합니다.";
        public static final String INVALID_MOBILE_FORMAT = "휴대전화번호 형식이 올바르지 않습니다.";
        public static final String INVALID_FILE_TYPE = "지원하지 않는 파일 형식입니다.";
        public static final String FILE_SIZE_EXCEEDED = "파일 크기가 제한을 초과했습니다.";
    }

    /**
     * 사업자 관련 상수
     */
    public static class Business {
        public static final int BUSINESS_NUMBER_LENGTH = 10; // 1234567890 형태
        public static final int BANK_ACCOUNT_MIN_LENGTH = 10;
        public static final int BANK_ACCOUNT_MAX_LENGTH = 20;
        
        // 은행 코드
        public static final String[] BANK_CODES = {
            "KB", "신한", "하나", "우리", "NH", "IBK", "KDB", "수협", "우체국", "새마을금고", "신협", "기타"
        };
    }

    /**
     * 페이징 관련 상수
     */
    public static class Paging {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_FIELD = "createdAt";
        public static final String DEFAULT_SORT_DIRECTION = "desc";
    }
}
