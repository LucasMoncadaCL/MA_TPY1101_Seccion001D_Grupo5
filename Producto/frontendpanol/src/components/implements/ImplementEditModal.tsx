import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import { fetchActiveCategories } from "../../services/activeCategoryService";
import { fetchLocations } from "../../services/locationService";
import { getErrorMessage } from "../../services/apiClient";
import type { ActiveCategoryOption } from "../../types/categoryActive";
import type { LocationOption } from "../../types/location";
import type { ImplementSummary } from "../../types/implement";

interface ImplementEditModalProps {
  implement: ImplementSummary | null;
  isOpen: boolean;
  saving: boolean;
  onClose: () => void;
  onSubmit: (payload: { id: number; name: string; categoryId: number | null; locationId: number }) => Promise<void>;
}

export function ImplementEditModal({
  implement,
  isOpen,
  saving,
  onClose,
  onSubmit,
}: ImplementEditModalProps) {
  const [name, setName] = useState("");
  const [categoryIdRaw, setCategoryIdRaw] = useState<string>("");
  const [locationIdRaw, setLocationIdRaw] = useState<string>("");

  const [categories, setCategories] = useState<ActiveCategoryOption[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [categoriesError, setCategoriesError] = useState<string | null>(null);

  const [locations, setLocations] = useState<LocationOption[]>([]);
  const [loadingLocations, setLoadingLocations] = useState(false);
  const [locationsError, setLocationsError] = useState<string | null>(null);

  const currentCategoryId = implement?.category?.id ?? null;
  const currentCategoryName = implement?.category?.name ?? null;
  const currentCategoryActive = implement?.category?.active ?? true;

  const currentLocationId = implement?.location?.id ?? null;
  const currentLocationName = implement?.location?.name ?? null;

  useEffect(() => {
    if (!isOpen || !implement) {
      return;
    }

    setName(implement.name);
    setCategories([]);
    setCategoriesError(null);
    setLoadingCategories(true);

    setLocations([]);
    setLocationsError(null);
    setLoadingLocations(true);

    fetchActiveCategories()
      .then((result) => {
        setCategories(result);

        if (!implement.category) {
          setCategoryIdRaw("");
          return;
        }

        const existsActive = result.some((category) => category.id === implement.category?.id);
        if (existsActive) {
          setCategoryIdRaw(String(implement.category.id));
          return;
        }

        setCategoryIdRaw(String(implement.category.id));
      })
      .catch((error) => {
        setCategoriesError(getErrorMessage(error, "No se pudo cargar las categorias."));
        setCategoryIdRaw(implement.category ? String(implement.category.id) : "");
      })
      .finally(() => setLoadingCategories(false));

    fetchLocations()
      .then((result) => {
        setLocations(result);
        setLocationIdRaw(implement.location ? String(implement.location.id) : "");
      })
      .catch((error) => {
        setLocationsError(getErrorMessage(error, "No se pudo cargar las ubicaciones."));
        setLocationIdRaw(implement.location ? String(implement.location.id) : "");
      })
      .finally(() => setLoadingLocations(false));
  }, [implement, isOpen]);

  const showInactiveCurrentCategory = useMemo(() => {
    if (!implement?.category) {
      return false;
    }
    return !currentCategoryActive && !categories.some((category) => category.id === currentCategoryId);
  }, [categories, currentCategoryActive, currentCategoryId, implement?.category]);

  const isSelectDisabled = useMemo(() => {
    if (loadingCategories) {
      return true;
    }
    if (categoriesError) {
      return false;
    }
    if (categories.length === 0 && !showInactiveCurrentCategory) {
      return true;
    }
    return false;
  }, [categories.length, categoriesError, loadingCategories, showInactiveCurrentCategory]);

  const helperText = useMemo(() => {
    if (loadingCategories) {
      return "Cargando categorias...";
    }
    if (categories.length === 0 && !showInactiveCurrentCategory) {
      return "No hay categorias disponibles";
    }
    return null;
  }, [categories.length, loadingCategories, showInactiveCurrentCategory]);

  const showMissingCurrentLocation = useMemo(() => {
    if (!implement?.location) {
      return false;
    }
    return !locations.some((location) => location.id === currentLocationId);
  }, [currentLocationId, implement?.location, locations]);

  const isLocationSelectDisabled = useMemo(() => {
    if (loadingLocations) {
      return true;
    }
    if (locationsError) {
      return false;
    }
    return locations.length === 0 && !showMissingCurrentLocation;
  }, [loadingLocations, locations.length, locationsError, showMissingCurrentLocation]);

  const locationHelperText = useMemo(() => {
    if (loadingLocations) {
      return "Cargando ubicaciones...";
    }
    if (locations.length === 0 && !showMissingCurrentLocation) {
      return "No hay ubicaciones disponibles";
    }
    return null;
  }, [loadingLocations, locations.length, showMissingCurrentLocation]);

  if (!isOpen || !implement) {
    return null;
  }

  const implementId = implement.id;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedName = name.trim();
    const categoryId = categoryIdRaw.trim() ? Number(categoryIdRaw) : null;
    const locationId = locationIdRaw.trim() ? Number(locationIdRaw) : NaN;

    if (!Number.isFinite(locationId)) {
      return;
    }

    await onSubmit({
      id: implementId,
      name: normalizedName,
      categoryId: Number.isFinite(categoryId) ? categoryId : null,
      locationId,
    });
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal">
        <h3>Editar implemento</h3>
        <p>Actualiza los datos del producto. La categoria puede quedar vacia.</p>

        <form onSubmit={handleSubmit}>
          <label htmlFor="implement-edit-name">Nombre</label>
          <input
            id="implement-edit-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Ej: Guantes latex"
            maxLength={100}
            required
          />

          <label htmlFor="implement-edit-category">Categoria</label>
          <select
            id="implement-edit-category"
            value={categoryIdRaw}
            onChange={(event) => setCategoryIdRaw(event.target.value)}
            disabled={isSelectDisabled}
          >
            {showInactiveCurrentCategory ? (
              <option value={String(currentCategoryId)} disabled>
                {currentCategoryName ?? "Categoria"} [Categoria inactiva]
              </option>
            ) : null}

            <option value="">Sin categoria</option>

            {categories.map((category) => (
              <option key={category.id} value={String(category.id)}>
                {category.name}
              </option>
            ))}
          </select>

          {helperText ? <p className="field-hint">{helperText}</p> : null}
          {categoriesError ? <p className="field-error">{categoriesError}</p> : null}

          <label htmlFor="implement-edit-location">Ubicacion</label>
          <select
            id="implement-edit-location"
            value={locationIdRaw}
            onChange={(event) => setLocationIdRaw(event.target.value)}
            disabled={isLocationSelectDisabled}
            required
          >
            {showMissingCurrentLocation ? (
              <option value={String(currentLocationId)} disabled>
                {currentLocationName ?? "Ubicacion"} [Ubicacion no disponible]
              </option>
            ) : (
              <option value="" disabled>
                Selecciona una ubicacion
              </option>
            )}

            {locations.map((location) => (
              <option key={location.id} value={String(location.id)}>
                {location.name}
              </option>
            ))}
          </select>

          {locationHelperText ? <p className="field-hint">{locationHelperText}</p> : null}
          {locationsError ? <p className="field-error">{locationsError}</p> : null}

          <div className="modal-actions">
            <button type="button" className="button button--ghost" onClick={onClose}>
              Cancelar
            </button>
            <button
              type="submit"
              className="button"
              disabled={
                saving ||
                name.trim().length === 0 ||
                locationIdRaw.trim().length === 0 ||
                (locations.length === 0 && !showMissingCurrentLocation)
              }
            >
              {saving ? "Guardando..." : "Guardar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
