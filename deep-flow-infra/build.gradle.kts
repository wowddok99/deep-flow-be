plugins {
    `java-library`
}

dependencies {
    implementation(project(":deep-flow-domain"))
    implementation(project(":deep-flow-application"))

    // JPA (domain에서 분리됨 → infra에서 직접 관리)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")

    // Redisson (분산 락)
    implementation("org.redisson:redisson-spring-boot-starter:3.27.0")

    // Bucket4j (Rate Limiting)
    api("com.bucket4j:bucket4j-core:8.7.0")
    implementation("com.bucket4j:bucket4j-redis:8.7.0")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // AOP
    api("org.springframework.boot:spring-boot-starter-aop")

    // Security (PasswordEncoder)
    implementation("org.springframework.security:spring-security-crypto")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
