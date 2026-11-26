import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";
import { bindings } from "../bindings.server";

function db() { const { DB } = bindings(); if (!DB) throw new Error("La base de datos aún no está disponible."); return DB; }

export const getFloreData = createServerFn({ method: "GET" }).handler(async () => {
  const database = db();
  const [products, customers, orders, sales, expenses, tasks, posts, mentions] = await Promise.all([
    database.prepare("SELECT * FROM products ORDER BY name").all(),
    database.prepare("SELECT * FROM customers ORDER BY total_spent DESC").all(),
    database.prepare("SELECT * FROM orders ORDER BY delivery_date, id DESC").all(),
    database.prepare("SELECT * FROM sales ORDER BY sale_date DESC, id DESC LIMIT 30").all(),
    database.prepare("SELECT * FROM expenses ORDER BY expense_date DESC, id DESC LIMIT 30").all(),
    database.prepare("SELECT * FROM tasks ORDER BY done, due_date").all(),
    database.prepare("SELECT * FROM content_posts ORDER BY id DESC").all(),
    database.prepare("SELECT * FROM social_mentions ORDER BY id DESC").all(),
  ]);
  return { products: products.results as Array<Record<string, any>>, customers: customers.results as Array<Record<string, any>>, orders: orders.results as Array<Record<string, any>>, sales: sales.results as Array<Record<string, any>>, expenses: expenses.results as Array<Record<string, any>>, tasks: tasks.results as Array<Record<string, any>>, posts: posts.results as Array<Record<string, any>>, mentions: mentions.results as Array<Record<string, any>> };
});

export const createProduct = createServerFn({ method: "POST" }).validator(z.object({ name:z.string().min(2), category:z.string(), sku:z.string(), stock:z.number().int().min(0), min_stock:z.number().int().min(0), unit_cost:z.number().int().min(0), unit_price:z.number().int().min(0), supplier:z.string() })).handler(async ({data}) => { const r=await db().prepare("INSERT INTO products (name,category,sku,stock,min_stock,unit_cost,unit_price,supplier) VALUES (?,?,?,?,?,?,?,?)").bind(data.name,data.category,data.sku,data.stock,data.min_stock,data.unit_cost,data.unit_price,data.supplier).run(); return {id:r.meta.last_row_id}; });
export const createCustomer = createServerFn({ method: "POST" }).validator(z.object({ name:z.string().min(2), phone:z.string(), email:z.string(), segment:z.string() })).handler(async ({data}) => { const r=await db().prepare("INSERT INTO customers (name,phone,email,segment) VALUES (?,?,?,?)").bind(data.name,data.phone,data.email,data.segment).run(); return {id:r.meta.last_row_id}; });
export const createOrder = createServerFn({ method: "POST" }).validator(z.object({ customer_name:z.string().min(2), occasion:z.string(), delivery_date:z.string(), total:z.number().int().min(0), notes:z.string() })).handler(async ({data}) => { const code=`FL-${Date.now().toString().slice(-5)}`; const r=await db().prepare("INSERT INTO orders (code,customer_name,occasion,delivery_date,total,notes) VALUES (?,?,?,?,?,?)").bind(code,data.customer_name,data.occasion,data.delivery_date,data.total,data.notes).run(); return {id:r.meta.last_row_id,code}; });
export const updateOrderStatus = createServerFn({ method: "POST" }).validator(z.object({ id:z.number().int(), status:z.string() })).handler(async ({data}) => { await db().prepare("UPDATE orders SET status=? WHERE id=?").bind(data.status,data.id).run(); return {ok:true}; });
export const updateStock = createServerFn({ method: "POST" }).validator(z.object({ id:z.number().int(), stock:z.number().int().min(0) })).handler(async ({data}) => { await db().prepare("UPDATE products SET stock=?, updated_at=datetime('now') WHERE id=?").bind(data.stock,data.id).run(); return {ok:true}; });
export const createTask = createServerFn({ method: "POST" }).validator(z.object({ title:z.string().min(2), due_date:z.string(), priority:z.string() })).handler(async ({data}) => { const r=await db().prepare("INSERT INTO tasks (title,due_date,priority) VALUES (?,?,?)").bind(data.title,data.due_date,data.priority).run(); return {id:r.meta.last_row_id}; });
export const toggleTask = createServerFn({ method: "POST" }).validator(z.object({ id:z.number().int(), done:z.boolean() })).handler(async ({data}) => { await db().prepare("UPDATE tasks SET done=? WHERE id=?").bind(data.done?1:0,data.id).run(); return {ok:true}; });
export const createSale = createServerFn({ method: "POST" }).validator(z.object({ description:z.string().min(2), amount:z.number().int().min(1), channel:z.string(), sale_date:z.string() })).handler(async ({data}) => { const r=await db().prepare("INSERT INTO sales (description,amount,channel,sale_date) VALUES (?,?,?,?)").bind(data.description,data.amount,data.channel,data.sale_date).run(); return {id:r.meta.last_row_id}; });
export const createExpense = createServerFn({ method: "POST" }).validator(z.object({ description:z.string().min(2), amount:z.number().int().min(1), category:z.string(), expense_date:z.string() })).handler(async ({data}) => { const r=await db().prepare("INSERT INTO expenses (description,amount,category,expense_date) VALUES (?,?,?,?)").bind(data.description,data.amount,data.category,data.expense_date).run(); return {id:r.meta.last_row_id}; });
