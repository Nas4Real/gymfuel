begin;

insert into auth.users (id)
values
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb');

insert into public.foods (
  id, user_id, name, preparation_state, calories_per_100g,
  protein_grams_per_100g, carbohydrate_grams_per_100g, fat_grams_per_100g
)
values
  ('aaaaaaaa-0000-4000-8000-000000000001', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'A food', 'cooked', 100, 20, 0, 2),
  ('bbbbbbbb-0000-4000-8000-000000000001', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'B food', 'cooked', 100, 20, 0, 2);

insert into public.profiles (user_id)
values
  ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb');

insert into public.nutrition_targets (id, user_id, effective_from, calories, protein_grams, carbohydrate_grams, fat_grams)
values
  ('aaaaaaaa-1000-4000-8000-000000000001', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', current_date, 2500, 180, 300, 70),
  ('bbbbbbbb-1000-4000-8000-000000000001', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', current_date, 2500, 180, 300, 70);

insert into public.food_entries (
  id, user_id, food_id, local_date, quantity_grams, status, consumed_at,
  food_name_snapshot, preparation_snapshot, calories_per_100g_snapshot,
  protein_grams_per_100g_snapshot, carbohydrate_grams_per_100g_snapshot,
  fat_grams_per_100g_snapshot
)
values
  ('aaaaaaaa-2000-4000-8000-000000000001', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', 'aaaaaaaa-0000-4000-8000-000000000001', current_date, 100, 'consumed', now(), 'A food', 'cooked', 100, 20, 0, 2),
  ('bbbbbbbb-2000-4000-8000-000000000001', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', 'bbbbbbbb-0000-4000-8000-000000000001', current_date, 100, 'consumed', now(), 'B food', 'cooked', 100, 20, 0, 2);

insert into public.water_entries (id, user_id, local_date, liters, logged_at)
values
  ('aaaaaaaa-3000-4000-8000-000000000001', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', current_date, 0.5, now()),
  ('bbbbbbbb-3000-4000-8000-000000000001', 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb', current_date, 0.5, now());

set local role authenticated;
set local request.jwt.claims = '{"sub":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","role":"authenticated"}';

with updated as (
  update public.foods
  set name = 'blocked'
  where id = 'bbbbbbbb-0000-4000-8000-000000000001'
  returning id
), updated_water as (
  update public.water_entries
  set liters = 1
  where id = 'bbbbbbbb-3000-4000-8000-000000000001'
  returning id
), results as (
  select
    (select count(*) from public.profiles) as visible_profiles,
    (select count(*) from public.nutrition_targets) as visible_targets,
    (select count(*) from public.foods) as visible_foods,
    (select count(*) from public.food_entries) as visible_entries,
    (select count(*) from public.water_entries) as visible_water_entries,
    (select count(*) from updated) as cross_user_updates,
    (select count(*) from updated_water) as cross_user_water_updates
)
select
  *,
  1 / case when
    visible_profiles = 1 and visible_targets = 1 and visible_foods = 1 and
    visible_entries = 1 and visible_water_entries = 1 and
    cross_user_updates = 0 and cross_user_water_updates = 0
  then 1 else 0 end as assertions_passed
from results;

rollback;
