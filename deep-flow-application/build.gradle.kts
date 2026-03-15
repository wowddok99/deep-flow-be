plugins {
    `java-library`
}

dependencies {
    api(project(":deep-flow-domain"))

    // domain Entity의 JPA 어노테이션 참조 시 경고 제거
    compileOnly("jakarta.persistence:jakarta.persistence-api")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.security:spring-security-crypto")

    // RateLimitPort의 Bucket 반환 타입 참조
    implementation("com.bucket4j:bucket4j-core:8.7.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
