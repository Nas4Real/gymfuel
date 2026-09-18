alter table public.nutrition_targets
  add column water_liters numeric(4, 2) not null default 2.5
  check (water_liters > 0 and water_liters <= 20);

create table public.water_entries (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  local_date date not null,
  liters numeric(5, 3) not null check (liters > 0 and liters <= 20),
  logged_at timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  revision bigint not null default 1 check (revision > 0)
);

create index water_entries_user_date_active_idx
  on public.water_entries (user_id, local_date, logged_at)
  where deleted_at is null;
create index water_entries_user_updated_idx
  on public.water_entries (user_id, updated_at, id);

create trigger water_entries_set_row_revision
before update on public.water_entries
for each row execute function private.set_row_revision();

alter table public.water_entries enable row level security;

create policy water_entries_select_own on public.water_entries for select to authenticated
  using ((select auth.uid()) = user_id);
create policy water_entries_insert_own on public.water_entries for insert to authenticated
  with check ((select auth.uid()) = user_id);
create policy water_entries_update_own on public.water_entries for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
create policy water_entries_delete_own on public.water_entries for delete to authenticated
  using ((select auth.uid()) = user_id);

grant select, insert, update, delete on public.water_entries to authenticated;
