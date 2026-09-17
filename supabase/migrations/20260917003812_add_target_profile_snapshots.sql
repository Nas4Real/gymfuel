alter table public.nutrition_targets
  add column age_years smallint check (age_years is null or age_years between 13 and 100),
  add column formula_sex text check (formula_sex is null or formula_sex in ('male', 'female')),
  add column height_centimeters numeric(6, 2) check (height_centimeters is null or height_centimeters between 100 and 250),
  add column weight_kilograms numeric(6, 2) check (weight_kilograms is null or weight_kilograms between 30 and 350),
  add column activity_multiplier numeric(4, 3) check (activity_multiplier is null or activity_multiplier between 1.2 and 2.5),
  add column surplus_calories numeric(6, 2) check (surplus_calories is null or surplus_calories between 0 and 1000);
