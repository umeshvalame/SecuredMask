# SecureMask

**Annotation-driven, role-aware JSON masking for Java 21 and Jackson.**

SecureMask is a small reference implementation demonstrating field-level masking at the JSON serialization boundary. It is **not yet security-audited or production-ready**. Use as a portfolio project and extend the hardening checklist before deploying to sensitive workloads.

## Requirements
- Java 21
- Maven 3.9+

## Build and test
```bash
mvn clean verify
```

## Usage
```java
record Customer(
    String name,
    @MaskField(revealTo = {"ADMIN"}) String ssn,
    @MaskField(revealTo = {"ADMIN"}, type = MaskType.LAST_FOUR) String account
) {}

RoleResolver resolver = () -> Set.of("SUPPORT"); // replace with trusted security context
ObjectMapper mapper = new ObjectMapper().registerModule(new SecureMaskModule(resolver));
System.out.println(mapper.writeValueAsString(new Customer("Jane", "123-45-6789", "1234567890")));
// {"name":"Jane","ssn":"********","account":"****7890"}
```

**Never use client-provided roles or JWT claims without validating the token.** The application is responsible for authentication, authorization, resource ownership checks, and preventing sensitive values from leaking through logs, alternate serializers, and other outputs.

## Architecture
`Domain DTO -> Jackson BeanSerializerModifier -> MaskingPropertyWriter -> RoleResolver + MaskType -> JSON`

The core library does not depend on Spring. A Spring Boot adapter can supply roles from the authenticated SecurityContext.

## Design choices
- Fail closed if roles are missing.
- Evaluate roles for every serialization operation, not when serializers are cached.
- Apply masking recursively to annotated properties in nested beans.
- Keep the DTO unchanged; modify only the JSON representation.
- Full masking emits a constant token. Partial masking supports String values.

## Known limitations / hardening backlog
- Field annotations on records should be covered with integration tests for the selected Jackson version.
- Add property-level deny/omit policies, `@JsonView` and mix-in compatibility tests.
- Review custom getters, `@JsonUnwrapped`, maps, arrays, polymorphic serialization, and `@JsonAnyGetter`.
- Validate non-string field behavior at startup; avoid partial leaks on errors.
- Support explicit role policies, ownership/ABAC checks, and deny-by-default configuration.
- Test concurrent requests with different roles and verify no cross-request leakage.
- Add Spring Security JWT adapter and full MVC integration tests.
- Run dependency scanning, SAST, mutation tests, and security review before production use.

## Suggested milestones
1. v0.1: core module + tests (this repository)
2. v0.2: Spring Boot auto-configuration and authenticated role resolution
3. v0.3: advanced policies and compatibility suite
4. v1.0: security-reviewed stable API

## License
-
