-- FLORE business data. Additive migration only.
CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT NOT NULL DEFAULT 'Flores', sku TEXT NOT NULL DEFAULT '', stock INTEGER NOT NULL DEFAULT 0, min_stock INTEGER NOT NULL DEFAULT 5, unit_cost INTEGER NOT NULL DEFAULT 0, unit_price INTEGER NOT NULL DEFAULT 0, supplier TEXT NOT NULL DEFAULT '', updated_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT NOT NULL DEFAULT '', email TEXT NOT NULL DEFAULT '', segment TEXT NOT NULL DEFAULT 'Nuevo', total_spent INTEGER NOT NULL DEFAULT 0, orders_count INTEGER NOT NULL DEFAULT 0, last_order TEXT NOT NULL DEFAULT '');
CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT NOT NULL UNIQUE, customer_id INTEGER, customer_name TEXT NOT NULL, occasion TEXT NOT NULL DEFAULT 'Otro', delivery_date TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'Pendiente', total INTEGER NOT NULL DEFAULT 0, notes TEXT NOT NULL DEFAULT '', created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS sales (id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT NOT NULL, amount INTEGER NOT NULL, channel TEXT NOT NULL DEFAULT 'Tienda', sale_date TEXT NOT NULL DEFAULT (date('now')));
CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT NOT NULL, amount INTEGER NOT NULL, category TEXT NOT NULL DEFAULT 'Operación', expense_date TEXT NOT NULL DEFAULT (date('now')));
CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, due_date TEXT NOT NULL DEFAULT '', done INTEGER NOT NULL DEFAULT 0, priority TEXT NOT NULL DEFAULT 'Normal');
CREATE TABLE IF NOT EXISTS content_posts (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, platform TEXT NOT NULL DEFAULT 'Instagram', status TEXT NOT NULL DEFAULT 'Borrador', scheduled_at TEXT NOT NULL DEFAULT '', reach INTEGER NOT NULL DEFAULT 0, engagement REAL NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS social_mentions (id INTEGER PRIMARY KEY AUTOINCREMENT, source TEXT NOT NULL, author TEXT NOT NULL, message TEXT NOT NULL, sentiment TEXT NOT NULL DEFAULT 'Neutral', created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE IF NOT EXISTS app_settings (key TEXT PRIMARY KEY, value TEXT NOT NULL);
INSERT OR IGNORE INTO products (id,name,category,sku,stock,min_stock,unit_cost,unit_price,supplier) VALUES
(1,'Rosa Freedom','Flores','FL-001',84,30,1800,4200,'Cultivos La Montaña'),
(2,'Eucalipto Baby Blue','Follajes','FL-002',18,20,2400,5500,'Verde Vivo'),
(3,'Tulipán holandés','Flores','FL-003',42,15,6200,12500,'Importadora Flora'),
(4,'Papel coreano marfil','Empaque','EM-001',9,12,2100,4800,'Empaques Nativa'),
(5,'Hortensia azul','Flores','FL-004',7,10,9500,18500,'Cultivos La Montaña'),
(6,'Cinta de algodón','Empaque','EM-002',26,10,900,2400,'Empaques Nativa');
INSERT OR IGNORE INTO customers (id,name,phone,email,segment,total_spent,orders_count,last_order) VALUES
(1,'Valentina Ríos','300 555 0182','valentina@email.com','Frecuente',485000,8,'2026-11-23'),
(2,'Andrés Molina','301 555 0127','andres@email.com','Corporativo',920000,12,'2026-11-22'),
(3,'Camila Torres','315 555 0194','camila@email.com','VIP',1250000,16,'2026-11-21'),
(4,'Mariana López','312 555 0165','mariana@email.com','Nuevo',165000,2,'2026-11-19');
INSERT OR IGNORE INTO orders (id,code,customer_id,customer_name,occasion,delivery_date,status,total,notes) VALUES
(1,'FL-2048',1,'Valentina Ríos','Cumpleaños','2026-11-26','En diseño',285000,'Rosas rosadas, tonos suaves'),
(2,'FL-2047',2,'Andrés Molina','Corporativo','2026-11-26','Pendiente',640000,'Entrega en recepción'),
(3,'FL-2046',3,'Camila Torres','Aniversario','2026-11-27','Listo',420000,'Incluir tarjeta personalizada'),
(4,'FL-2045',4,'Mariana López','Agradecimiento','2026-11-27','En camino',165000,'');
INSERT OR IGNORE INTO sales (id,description,amount,channel,sale_date) VALUES
(1,'Bouquet Aurora',285000,'WhatsApp','2026-11-25'),(2,'Centro de mesa botánico',420000,'Tienda','2026-11-25'),(3,'Ramo corporativo x 3',640000,'Instagram','2026-11-24'),(4,'Arreglo Serenidad',165000,'WhatsApp','2026-11-23');
INSERT OR IGNORE INTO expenses (id,description,amount,category,expense_date) VALUES
(1,'Compra de flor fresca',380000,'Inventario','2026-11-25'),(2,'Empaques y cintas',95000,'Materiales','2026-11-24'),(3,'Domicilios',48000,'Logística','2026-11-23');
INSERT OR IGNORE INTO tasks (id,title,due_date,done,priority) VALUES
(1,'Preparar pedidos de la mañana','2026-11-26',0,'Alta'),(2,'Confirmar flores con proveedor','2026-11-26',0,'Alta'),(3,'Fotografiar colección de temporada','2026-11-27',0,'Normal'),(4,'Actualizar precios de follajes','2026-11-28',1,'Normal');
INSERT OR IGNORE INTO content_posts (id,title,platform,status,scheduled_at,reach,engagement) VALUES
(1,'Colección Flores de noviembre','Instagram','Programado','2026-11-26 10:00',4820,6.8),(2,'Detrás de cada bouquet','TikTok','Borrador','2026-11-27 17:00',0,0),(3,'Ideas para regalar sin ocasión','Instagram','Publicado','2026-11-24 12:00',8210,8.2);
INSERT OR IGNORE INTO social_mentions (id,source,author,message,sentiment) VALUES
(1,'Instagram','@laura.m','El ramo llegó precioso, superó mis expectativas 🌷','Positivo'),(2,'WhatsApp','Cliente frecuente','¿Tienen disponibilidad para un arreglo de aniversario mañana?','Intención de compra'),(3,'Instagram','@mariaf','Me encantan los colores de la nueva colección','Positivo');
INSERT OR IGNORE INTO app_settings (key,value) VALUES ('business_name','FLORE Atelier'),('currency','COP'),('timezone','America/Bogota');
