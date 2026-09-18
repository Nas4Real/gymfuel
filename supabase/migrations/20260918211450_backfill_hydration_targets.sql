update public.nutrition_targets
set water_liters = round(weight_kilograms * 0.035, 1)
where weight_kilograms is not null;
