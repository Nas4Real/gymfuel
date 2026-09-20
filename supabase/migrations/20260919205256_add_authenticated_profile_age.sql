alter table public.profiles
  add column age_years integer
  check (age_years is null or age_years between 13 and 100),
  add constraint profiles_body_inputs_complete check (
    (
      age_years is null and formula_sex is null and height_centimeters is null and
      weight_kilograms is null and activity_multiplier is null and surplus_calories is null
    )
    or
    (
      age_years is not null and formula_sex is not null and height_centimeters is not null and
      weight_kilograms is not null and activity_multiplier is not null and surplus_calories is not null
    )
  );

comment on column public.profiles.age_years is
  'Current calculator age. Historical target inputs remain snapshotted on nutrition_targets.';
