create table if not exists bill_category (
  id integer primary key,
  category_code text,
  name text not null,
  source text default 'manual',
  enabled integer not null default 1,
  updated_at text default (datetime('now','localtime'))
);

create table if not exists bill_record (
  id integer primary key autoincrement,
  trade_time text,
  trade_type text,
  income_expense_type text,
  counterparty text,
  product_name text,
  amount real,
  pay_method text,
  trade_status text,
  trade_no text,
  merchant_order_no text,
  remark text,
  category_id integer,
  category_confidence real,
  category_source text default 'import-default',
  category_sync_status text,
  category_sync_reason text,
  category_sync_at text,
  settlement_included integer not null default 1,
  source_file_name text,
  import_key text,
  created_at text default (datetime('now','localtime'))
);

create table if not exists bill_category_sync_log (
  id integer primary key autoincrement,
  sync_batch_no text,
  bill_id integer,
  import_key text,
  requested_category_code text,
  resolved_category_id integer,
  confidence real,
  reason text,
  status text,
  message text,
  created_at text default (datetime('now','localtime'))
);

create table if not exists import_history (
  id integer primary key autoincrement,
  source_file_name text,
  total_count integer not null default 0,
  success_count integer not null default 0,
  fail_count integer not null default 0,
  message text,
  created_at text default (datetime('now','localtime'))
);

create index if not exists idx_bill_record_trade_time on bill_record(trade_time);
create unique index if not exists uk_bill_record_import_key on bill_record(import_key);
create unique index if not exists uk_bill_category_category_code on bill_category(category_code);
create index if not exists idx_bill_category_sync_log_batch_no on bill_category_sync_log(sync_batch_no);

insert or ignore into bill_category (id, category_code, name, source, enabled) values (1, 'FOOD', 'Food', 'manual', 1);
insert or ignore into bill_category (id, category_code, name, source, enabled) values (2, 'TRANSPORT', 'Transport', 'manual', 1);
insert or ignore into bill_category (id, category_code, name, source, enabled) values (3, 'SHOPPING', 'Shopping', 'manual', 1);
