create schema if not exists private;

create or replace function private.set_row_revision()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
  new.updated_at = now();
  new.revision = old.revision + 1;
  return new;
end;
$$;

revoke all on function private.set_row_revision() from public, anon, authenticated;

create table public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  time_zone text not null default 'UTC',
  unit_system text not null default 'metric' check (unit_system in ('metric', 'imperial')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  revision bigint not null default 1 check (revision > 0)
);

create table public.nutrition_targets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  effective_from date not null,
  goal text not null default 'muscle_gain' check (goal in ('muscle_gain', 'maintain', 'fat_loss')),
  calories numeric(8, 2) not null check (calories between 0 and 20000),
  protein_grams numeric(8, 3) not null check (protein_grams between 0 and 1000),
  carbohydrate_grams numeric(8, 3) not null check (carbohydrate_grams between 0 and 2000),
  fat_grams numeric(8, 3) not null check (fat_grams between 0 and 1000),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  revision bigint not null default 1 check (revision > 0)
);

create table public.food_templates (
  id uuid primary key,
  name text not null check (char_length(name) between 1 and 120),
  brand text,
  preparation_state text not null check (preparation_state in ('raw', 'cooked', 'dry', 'drained', 'custom')),
  calories_per_100g numeric(8, 3) not null check (calories_per_100g between 0 and 1000),
  protein_grams_per_100g numeric(8, 3) not null check (protein_grams_per_100g between 0 and 100),
  carbohydrate_grams_per_100g numeric(8, 3) not null check (carbohydrate_grams_per_100g between 0 and 100),
  fat_grams_per_100g numeric(8, 3) not null check (fat_grams_per_100g between 0 and 100),
  image_path text check (image_path is null or char_length(image_path) <= 500),
  created_at timestamptz not null default now()
);

create table public.foods (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  source_template_id uuid references public.food_templates(id) on delete set null,
  name text not null check (char_length(name) between 1 and 120),
  brand text check (brand is null or char_length(brand) <= 120),
  preparation_state text not null check (preparation_state in ('raw', 'cooked', 'dry', 'drained', 'custom')),
  calories_per_100g numeric(8, 3) not null check (calories_per_100g between 0 and 1000),
  protein_grams_per_100g numeric(8, 3) not null check (protein_grams_per_100g between 0 and 100),
  carbohydrate_grams_per_100g numeric(8, 3) not null check (carbohydrate_grams_per_100g between 0 and 100),
  fat_grams_per_100g numeric(8, 3) not null check (fat_grams_per_100g between 0 and 100),
  image_path text check (image_path is null or char_length(image_path) <= 500),
  is_favorite boolean not null default false,
  notes text check (notes is null or char_length(notes) <= 2000),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  revision bigint not null default 1 check (revision > 0)
);

create table public.food_entries (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  food_id uuid references public.foods(id) on delete set null,
  local_date date not null,
  quantity_grams numeric(10, 3) not null check (quantity_grams > 0 and quantity_grams <= 100000),
  status text not null check (status in ('planned', 'consumed', 'skipped')),
  meal_label text check (meal_label is null or char_length(meal_label) <= 80),
  consumed_at timestamptz,
  food_name_snapshot text not null check (char_length(food_name_snapshot) between 1 and 120),
  preparation_snapshot text not null check (preparation_snapshot in ('raw', 'cooked', 'dry', 'drained', 'custom')),
  image_path_snapshot text check (image_path_snapshot is null or char_length(image_path_snapshot) <= 500),
  calories_per_100g_snapshot numeric(8, 3) not null check (calories_per_100g_snapshot between 0 and 1000),
  protein_grams_per_100g_snapshot numeric(8, 3) not null check (protein_grams_per_100g_snapshot between 0 and 100),
  carbohydrate_grams_per_100g_snapshot numeric(8, 3) not null check (carbohydrate_grams_per_100g_snapshot between 0 and 100),
  fat_grams_per_100g_snapshot numeric(8, 3) not null check (fat_grams_per_100g_snapshot between 0 and 100),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  revision bigint not null default 1 check (revision > 0),
  check (
    (status = 'consumed' and consumed_at is not null)
    or (status in ('planned', 'skipped'))
  )
);

create unique index nutrition_targets_user_effective_active_idx
  on public.nutrition_targets (user_id, effective_from)
  where deleted_at is null;
create index nutrition_targets_user_updated_idx
  on public.nutrition_targets (user_id, updated_at, id);
create index foods_user_updated_idx on public.foods (user_id, updated_at, id);
create index foods_user_active_name_idx
  on public.foods (user_id, lower(name))
  where deleted_at is null;
create index foods_source_template_id_idx on public.foods (source_template_id);
create index food_entries_user_date_idx
  on public.food_entries (user_id, local_date, status)
  where deleted_at is null;
create index food_entries_user_updated_idx
  on public.food_entries (user_id, updated_at, id);
create index food_entries_food_id_idx on public.food_entries (food_id);

create trigger profiles_set_row_revision
before update on public.profiles
for each row execute function private.set_row_revision();
create trigger nutrition_targets_set_row_revision
before update on public.nutrition_targets
for each row execute function private.set_row_revision();
create trigger foods_set_row_revision
before update on public.foods
for each row execute function private.set_row_revision();
create trigger food_entries_set_row_revision
before update on public.food_entries
for each row execute function private.set_row_revision();

alter table public.profiles enable row level security;
alter table public.nutrition_targets enable row level security;
alter table public.food_templates enable row level security;
alter table public.foods enable row level security;
alter table public.food_entries enable row level security;

create policy profiles_select_own on public.profiles for select to authenticated
  using ((select auth.uid()) = user_id);
create policy profiles_insert_own on public.profiles for insert to authenticated
  with check ((select auth.uid()) = user_id);
create policy profiles_update_own on public.profiles for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
create policy profiles_delete_own on public.profiles for delete to authenticated
  using ((select auth.uid()) = user_id);

create policy nutrition_targets_select_own on public.nutrition_targets for select to authenticated
  using ((select auth.uid()) = user_id);
create policy nutrition_targets_insert_own on public.nutrition_targets for insert to authenticated
  with check ((select auth.uid()) = user_id);
create policy nutrition_targets_update_own on public.nutrition_targets for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
create policy nutrition_targets_delete_own on public.nutrition_targets for delete to authenticated
  using ((select auth.uid()) = user_id);

create policy food_templates_read_authenticated on public.food_templates for select to authenticated
  using (true);

create policy foods_select_own on public.foods for select to authenticated
  using ((select auth.uid()) = user_id);
create policy foods_insert_own on public.foods for insert to authenticated
  with check ((select auth.uid()) = user_id);
create policy foods_update_own on public.foods for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
create policy foods_delete_own on public.foods for delete to authenticated
  using ((select auth.uid()) = user_id);

create policy food_entries_select_own on public.food_entries for select to authenticated
  using ((select auth.uid()) = user_id);
create policy food_entries_insert_own on public.food_entries for insert to authenticated
  with check ((select auth.uid()) = user_id);
create policy food_entries_update_own on public.food_entries for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
create policy food_entries_delete_own on public.food_entries for delete to authenticated
  using ((select auth.uid()) = user_id);

grant usage on schema public to authenticated;
grant select, insert, update, delete on public.profiles to authenticated;
grant select, insert, update, delete on public.nutrition_targets to authenticated;
grant select on public.food_templates to authenticated;
grant select, insert, update, delete on public.foods to authenticated;
grant select, insert, update, delete on public.food_entries to authenticated;

insert into public.food_templates (
  id, name, brand, preparation_state, calories_per_100g,
  protein_grams_per_100g, carbohydrate_grams_per_100g,
  fat_grams_per_100g, image_path
) values
  ('10000000-0000-4000-8000-000000000001', 'Chicken breast', null, 'cooked', 165, 31, 0, 3.6, 'templates/chicken-breast.webp'),
  ('10000000-0000-4000-8000-000000000002', 'White rice', null, 'cooked', 130, 2.7, 28.2, 0.3, 'templates/white-rice.webp'),
  ('10000000-0000-4000-8000-000000000003', 'Rolled oats', null, 'dry', 389, 16.9, 66.3, 6.9, 'templates/rolled-oats.webp'),
  ('10000000-0000-4000-8000-000000000004', 'Whole egg', null, 'cooked', 155, 12.6, 1.1, 10.6, 'templates/whole-eggs.webp'),
  ('10000000-0000-4000-8000-000000000005', 'Banana', null, 'raw', 89, 1.1, 22.8, 0.3, 'templates/banana.webp'),
  ('10000000-0000-4000-8000-000000000006', 'Tuna', 'Canned in water', 'drained', 116, 25.5, 0, 0.8, 'templates/tuna.webp'),
  ('10000000-0000-4000-8000-000000000007', 'Lean ground beef', null, 'cooked', 250, 26, 0, 15, 'templates/ground-beef.webp'),
  ('10000000-0000-4000-8000-000000000008', 'Greek yogurt', '0% fat', 'custom', 59, 10.3, 3.6, 0.4, 'templates/greek-yogurt.webp')
on conflict (id) do nothing;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
  ('food-images', 'food-images', false, 5242880, array['image/jpeg', 'image/png', 'image/webp']),
  ('food-template-images', 'food-template-images', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

create policy food_images_select_own on storage.objects for select to authenticated
  using (
    bucket_id = 'food-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );
create policy food_images_insert_own on storage.objects for insert to authenticated
  with check (
    bucket_id = 'food-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );
create policy food_images_update_own on storage.objects for update to authenticated
  using (
    bucket_id = 'food-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  )
  with check (
    bucket_id = 'food-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );
create policy food_images_delete_own on storage.objects for delete to authenticated
  using (
    bucket_id = 'food-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

create policy food_template_images_read on storage.objects for select to authenticated
  using (bucket_id = 'food-template-images');
