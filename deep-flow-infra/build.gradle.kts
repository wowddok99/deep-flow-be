plugins {
    `java-library`
}

dependencies {
    implementation(project(":deep-flow-domain"))
    implementation(project(":deep-flow-application"))

    // JPA
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

    // Web (SSE support)
    implementation("org.springframework:spring-webmvc")

    // AOP
    api("org.springframework.boot:spring-boot-starter-aop")

    // Security (PasswordEncoder)
    implementation("org.springframework.security:spring-security-crypto")

    // S3 (Cloudflare R2)
    implementation(platform("software.amazon.awssdk:bom:2.25.16"))
    implementation("software.amazon.awssdk:s3")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
