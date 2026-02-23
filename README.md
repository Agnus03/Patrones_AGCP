# Patrones de Software
Andres Giovanny Garcia Lopez
Carlos Santiago Picon Diaz

# Sistema de Gestión de Cadena de Suministro

Este repositorio contiene un proyecto para diseñar e implementar un **sistema de gestión de cadena de suministro** centrado en trazabilidad, optimización logística y analítica predictiva. [ibm](https://www.ibm.com/es-es/think/topics/supply-chain-management)

## Objetivo del proyecto

- Gestionar el flujo de productos desde el fabricante hasta el cliente final con visibilidad de punta a punta. [oracle](https://www.oracle.com/latam/scm/what-is-supply-chain-management/)
- Reducir costos operativos mediante optimización de rutas, almacenamiento e inventarios. [lanjatrans](https://lanjatrans.com/big-data-y-analisis-predictivo-en-la-optimizacion-logistica/)
- Anticipar la demanda usando modelos de análisis predictivo para mejorar la planificación. [mecalux.com](https://www.mecalux.com.co/blog/analisis-predictivo)
- Integrar dispositivos IoT para monitoreo en tiempo real de ubicación y condiciones de los productos. [logisticaevolutiva.puntanetwork](https://logisticaevolutiva.puntanetwork.com/tecnologias-emergentes/papel-crucial-iot-trazabilidad-productos-innovacion-seguimiento/)

## Alcance funcional

El sistema se enfocará en cuatro grandes módulos:

1. **Seguimiento de productos**  
   - Registro de cada producto desde su salida de planta hasta la entrega al cliente final.  
   - Trazabilidad en tiempo real de ubicación y estado usando datos IoT (por ejemplo, geolocalización, temperatura, humedad). [menttoriza](https://menttoriza.com/noticias/trazabilidad-tiempo-real-rfid-iot/)

2. **Optimización de rutas y almacenamiento**  
   - Propuesta de rutas de transporte más eficientes según distancia, tráfico y ventanas de entrega. [lanjatrans](https://lanjatrans.com/big-data-y-analisis-predictivo-en-la-optimizacion-logistica/)
   - Recomendaciones para uso de espacio en almacén y rotación de inventario, buscando minimizar tiempos y costos. [ibm](https://www.ibm.com/mx-es/think/topics/supply-chain-management)

3. **Predicción de demanda (análisis predictivo)**  
   - Modelos que estimen la demanda futura por producto, cliente o región a partir de datos históricos y variables externas. [mecalux.com](https://www.mecalux.com.co/blog/analisis-predictivo)
   - Ajuste automático de niveles de inventario y planificación de producción en función de esas previsiones. [mecalux.com](https://www.mecalux.com.co/blog/analisis-predictivo)

4. **Integración con IoT para monitoreo en tiempo real**  
   - Conexión con sensores, etiquetas RFID o dispositivos embebidos en vehículos y contenedores. [progrow](https://www.progrow.io/es/blog-noticias/mejore-la-experiencia-y-la-trazabilidad-en-la-planta-de-produccion-con-la-integracion-de-iot)
   - Recepción continua de datos para alimentar paneles de control y generar alertas (retrasos, condiciones fuera de rango, etc.). [logisticaevolutiva.puntanetwork](https://logisticaevolutiva.puntanetwork.com/tecnologias-emergentes/papel-crucial-iot-trazabilidad-productos-innovacion-seguimiento/)

## Tecnologías esperadas

(Adaptar según avances reales del proyecto.)

- Backend para lógica de negocio, APIs y conexión con dispositivos IoT.  
- Base de datos relacional o NoSQL para trazabilidad, inventarios y eventos de sensores. [panorama-consulting](https://www.panorama-consulting.com/es/que-es-un-sistema-de-gestion-de-la-cadena-de-suministro/)
- Servicios de analítica/ML para modelos de predicción de demanda y optimización logística. [lanjatrans](https://lanjatrans.com/big-data-y-analisis-predictivo-en-la-optimizacion-logistica/)
- Frontend o dashboards para visualización de rutas, estados de pedidos e indicadores de desempeño. [ibm](https://www.ibm.com/mx-es/think/topics/supply-chain-management)

## Casos de uso principales

- Un operador logístico consulta el recorrido de un lote y ve en qué eslabón de la cadena se encuentra cada producto. [iebschool](https://www.iebschool.com/hub/cadena-gestion-suministro-negocios-internacionales/)
- El planificador de transporte genera la mejor ruta diaria para su flota, considerando tiempos de entrega y restricciones de capacidad. [lanjatrans](https://lanjatrans.com/big-data-y-analisis-predictivo-en-la-optimizacion-logistica/)
- El área de planificación recibe una previsión de demanda de los próximos meses y ajusta producción e inventario objetivo. [mecalux.com](https://www.mecalux.com.co/blog/analisis-predictivo)
- Un responsable de calidad recibe una alerta cuando un envío supera la temperatura permitida durante el transporte gracias a sensores IoT. [progrow](https://www.progrow.io/es/blog-noticias/mejore-la-experiencia-y-la-trazabilidad-en-la-planta-de-produccion-con-la-integracion-de-iot)

## Estado del proyecto

El proyecto se encuentra en fase inicial de diseño funcional y definición de arquitectura.  
A medida que se desarrollen los módulos, se irán documentando requisitos técnicos, endpoints, modelos de datos y ejemplos de uso en este mismo repositorio. [panorama-consulting](https://www.panorama-consulting.com/es/que-es-un-sistema-de-gestion-de-la-cadena-de-suministro/)
