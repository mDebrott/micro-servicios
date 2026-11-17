-- Ejecutar UNA vez en la base "aeronaves" (después del primer arranque, cuando las tablas ya existen).
-- Garantizan, a nivel de base de datos, un solo vuelo ABIERTO por aeronave y por piloto.
CREATE UNIQUE INDEX IF NOT EXISTS uq_vuelo_abierto_aeronave
  ON registros_vuelo (aeronave_id) WHERE estado = 'ABIERTO';
CREATE UNIQUE INDEX IF NOT EXISTS uq_vuelo_abierto_piloto
  ON registros_vuelo (piloto_id) WHERE estado = 'ABIERTO';
