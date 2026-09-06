# Botica San Jose - Demo academica de Session Fixation

Sistema basico de gestion para una botica (login, panel, inventario) construido en
**Spring Boot**, pensado como material de apoyo para la exposicion sobre
**Session Fixation**. Incluye un interruptor para mostrar en vivo el comportamiento
**vulnerable** y el comportamiento **corregido**.

> ⚠️ **Uso exclusivamente educativo.** Esta aplicacion contiene una vulnerabilidad
> intencional. No la reutilices como base de un sistema real, no cargues datos
> reales de pacientes/clientes, y si la subes a un servidor web hazlo en un
> entorno aislado y de forma temporal (solo mientras dure la demo/sustentacion).

## Requisitos

- Java 17+
- Maven 3.8+ (o el wrapper de tu IDE)

## Como ejecutar

```bash
mvn spring-boot:run
```

La app queda en `http://localhost:8080`. Se crea automaticamente:

- **Usuario administrador:** `admin`
- **Contrasena:** `Admin123*`

(Se ve tambien impreso en la consola al iniciar).

## Estructura del proyecto

```
src/main/java/com/botica/demo/
├── BoticaApplication.java
├── config/DataSeeder.java          -> crea el admin y productos de ejemplo
├── controller/AuthController.java  -> login/logout (AQUI esta la logica de la demo)
├── controller/DashboardController.java
├── controller/ProductoController.java
├── filter/SessionAuthFilter.java   -> protege rutas segun la sesion
├── model/Usuario.java
├── model/Producto.java
└── repository/...
src/main/resources/
├── application.properties          -> aqui se activa/desactiva la vulnerabilidad
└── templates/ (login, dashboard, productos)
```

## El interruptor de la demo

En `application.properties`:

```properties
app.security.session-fixation-vulnerable=true
```

- `true`  -> el login **no regenera** el ID de sesion (vulnerable, para la demo).
- `false` -> el login llama a `request.changeSessionId()` (corregido).

La logica esta en `AuthController.login(...)`. Cambia el valor, reinicia la app
y repite la prueba para mostrar la diferencia en vivo.

## Como demostrar la vulnerabilidad (paso a paso)

Con `session-fixation-vulnerable=true`:

1. Abre el navegador en modo incognito y entra a `http://localhost:8080/login`.
   El servidor te asigna una cookie `JSESSIONID` **antes** de que inicies sesion.
   Copia ese valor desde las DevTools (Application > Cookies).
2. Simula al "atacante": en otra ventana/perfil del navegador, entra tambien a
   `/login` y, con las DevTools, **edita manualmente** el valor de su cookie
   `JSESSIONID` para que sea el mismo que copiaste en el paso 1 (esto simula
   que el atacante logro fijar ese identificador en el navegador de la victima).
3. En la primera ventana (la "victima"), inicia sesion con `admin / Admin123*`.
4. Ve a `/dashboard` y anota el `ID de sesion actual` que se muestra en pantalla.
5. Sin volver a hacer login, recarga `/dashboard` en la ventana del "atacante"
   (la que tiene la cookie copiada). **Va a poder ver el panel como si fuera el
   administrador**, porque el ID de sesion nunca cambio tras el login.

Ahora cambia `app.security.session-fixation-vulnerable=false`, reinicia la app
y repite los mismos pasos: en el paso 4 el ID de sesion habra cambiado respecto
al que el "atacante" copio, y en el paso 5 la ventana del "atacante" sera
redirigida a `/login` (su cookie fijada ya no sirve).

### Alternativa con `curl` (sin navegador)

```bash
# 1) El atacante obtiene un JSESSIONID valido antes de loguearse
curl -i -c atacante.txt http://localhost:8080/login

# 2) Alguien usa ESE MISMO id para loguearse (la victima)
curl -i -b atacante.txt -c atacante.txt \
  -d "username=admin&password=Admin123*" \
  http://localhost:8080/login

# 3) El atacante reutiliza su cookie original para entrar al panel
curl -i -b atacante.txt http://localhost:8080/dashboard
```

Con el modo vulnerable, el paso 3 devuelve el HTML del panel (200 OK).
Con el modo corregido, el paso 3 redirige a `/login` (302).

## Notas de seguridad para cuando ya no sea una demo

Si en algun momento quieres partir de este proyecto para algo real, como minimo:

- Deja `app.security.session-fixation-vulnerable=false`.
- Activa `server.servlet.session.cookie.secure=true` (requiere HTTPS).
- Reemplaza la base H2 en memoria por una base de datos persistente.
- Agrega control de intentos de login, expiracion de sesion y HTTPS obligatorio.
