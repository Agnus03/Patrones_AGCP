# Patrones de Software
## Andres Giovanny Garcia Lopez
## Carlos Santiago Picon Diaz

---

📦 Sistema de Gestión de Cadena de Suministro

Este repositorio contiene el diseño e implementación de un Sistema de Gestión de Cadena de Suministro, enfocado en trazabilidad de productos, optimización logística y analítica predictiva, con soporte para monitoreo en tiempo real mediante IoT.

El proyecto busca mejorar la visibilidad de los procesos logísticos, reducir costos operativos y apoyar la toma de decisiones estratégicas a lo largo de toda la cadena de suministro.

🎯 Objetivo del Proyecto

Gestionar el flujo de productos desde el fabricante hasta el cliente final, con visibilidad de punta a punta.

Reducir costos operativos mediante la optimización de rutas, almacenamiento e inventarios.

Anticipar la demanda utilizando modelos de análisis predictivo para mejorar la planificación.

Integrar dispositivos IoT para el monitoreo en tiempo real de la ubicación y condiciones de los productos.

🔍 Alcance Funcional

El sistema se estructura en cuatro módulos principales:

📍 Seguimiento de Productos

Registro y control de cada producto desde su salida de planta hasta la entrega al cliente final.

Trazabilidad en tiempo real del estado y ubicación mediante datos IoT (geolocalización, temperatura, humedad, entre otros).

🚚 Optimización de Rutas y Almacenamiento

Propuesta de rutas de transporte más eficientes considerando distancia, tráfico y ventanas de entrega.

Recomendaciones para el uso óptimo del espacio en almacenes y la rotación de inventarios, buscando minimizar tiempos y costos logísticos.

📊 Predicción de Demanda (Análisis Predictivo)

Modelos que estiman la demanda futura por producto, cliente o región a partir de datos históricos y variables externas.

Ajuste de niveles de inventario y apoyo a la planificación de producción según las previsiones generadas.

🌐 Integración con IoT para Monitoreo en Tiempo Real

Conexión con sensores, etiquetas RFID o dispositivos instalados en vehículos y contenedores.

Recepción continua de datos para alimentar paneles de control y generar alertas ante retrasos o condiciones fuera de rango.

🛠️ Tecnologías Esperadas

(Sujetos a cambios según el avance del proyecto)

Backend para la lógica de negocio, APIs y conexión con dispositivos IoT.

Base de datos relacional o NoSQL para trazabilidad, inventarios y eventos de sensores.

Servicios de analítica y machine learning para predicción de demanda y optimización logística.

Frontend o dashboards para la visualización de rutas, estados de pedidos e indicadores de desempeño.

👥 Casos de Uso Principales

Un operador logístico consulta el recorrido de un lote y verifica en qué etapa de la cadena se encuentra cada producto.

El planificador de transporte genera rutas diarias óptimas para la flota considerando tiempos de entrega y restricciones de capacidad.

El área de planificación recibe previsiones de demanda para ajustar producción e inventarios.

Un responsable de calidad recibe alertas cuando un envío supera los rangos permitidos de temperatura durante el transporte gracias a sensores IoT.

📌 Estado del Proyecto

🟡 El proyecto se encuentra en una fase inicial de diseño funcional y definición de arquitectura.
A medida que se desarrollen los distintos módulos, se documentarán en este repositorio los requisitos técnicos, modelos de datos, endpoints y ejemplos de uso.
---
