# Mejoras en Manejo de Errores de Validación

## Problema Identificado

En `GlobalExceptionHandler.handleValidation()`, se construía un mapa de errores pero no se incluía en la respuesta:

```java
// ANTES: El mapa se construía pero se descartaba
Map<String, String> errors = new HashMap<>();
ex.getBindingResult().getAllErrors().forEach(error -> {
    String field = ((FieldError) error).getField();
    String message = error.getDefaultMessage();
    errors.put(field, message);
});
return ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(ApiResponse.error("Errores de validación")); // ❌ errors no se incluye
```

**Consecuencias:**
1. El cliente recibía solo el mensaje sin detalles de qué campos fallaban
2. Validaciones a nivel de objeto (`@ClassLevelConstraint`) causaban `ClassCastException` al hacer cast a `FieldError`

## Soluciones Implementadas

### 1. Nuevo método en ApiResponse

Agregué un factory method para incluir data en respuestas de error:

```java
public static <T> ApiResponse<T> error(String message, T data) {
    return new ApiResponse<>(false, message, data);
}
```

### 2. Manejo de FieldError y ObjectError

Actualicé `handleValidation()` con validación instanceof:

```java
ex.getBindingResult().getAllErrors().forEach(error -> {
    String message = error.getDefaultMessage();
    
    // Errores de campo individual
    if (error instanceof FieldError) {
        String field = ((FieldError) error).getField();
        errors.put(field, message);
    } 
    // Errores globales a nivel de objeto
    else if (error instanceof ObjectError) {
        String objectName = error.getObjectName();
        errors.put("global:" + objectName, message);
    }
});
```

### 3. Respuesta Mejorada

```java
return ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(ApiResponse.error("Errores de validación", errors)); // ✅ errors incluido
```

## Ejemplo de Respuesta

### Errores de Campo

**Request con validación inválida:**
```json
POST /api/v1/users
{
  "email": "invalid-email",
  "password": "123"
}
```

**Response:**
```json
{
  "success": false,
  "message": "Errores de validación",
  "data": {
    "email": "Formato de correo inválido",
    "password": "La contraseña debe tener al menos 8 caracteres"
  },
  "timestamp": "2026-05-05T10:30:45"
}
```

### Errores Globales (si existen validaciones a nivel de clase)

```json
{
  "success": false,
  "message": "Errores de validación",
  "data": {
    "name": "El nombre es obligatorio",
    "global:userRequest": "La fecha de inicio debe ser anterior a la de fin"
  },
  "timestamp": "2026-05-05T10:30:45"
}
```

## Beneficios

✅ **Cliente recibe detalles completos** de qué campos fallaron y por qué  
✅ **Seguridad**: Sin riesgo de `ClassCastException` al mezclar tipos de validación  
✅ **UX mejorada**: El frontend puede mostrar errores específicos en cada campo  
✅ **Mantenibilidad**: Código robusto con validación de tipos explícita  

## Archivos Modificados

- [`ApiResponse.java`](src/main/java/com/cocobongo/cerveceria/common/dto/ApiResponse.java) - Agregado factory method
- [`GlobalExceptionHandler.java`](src/main/java/com/cocobongo/cerveceria/common/exception/GlobalExceptionHandler.java) - Mejorado manejo de validaciones
