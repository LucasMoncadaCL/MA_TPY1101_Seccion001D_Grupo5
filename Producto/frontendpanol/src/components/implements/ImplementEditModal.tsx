import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import { fetchActiveCategories } from "../../services/activeCategoryService";
import { getApiErrorPayload, getErrorMessage } from "../../services/apiClient";
import { fetchImplementById, updateImplement } from "../../services/implementService";
import { fetchLocations } from "../../services/locationService";
import type { ActiveCategoryOption } from "../../types/categoryActive";
import type { LocationOption } from "../../types/location";
import type { ImplementDetail } from "../../types/implement";

type ItemType = "consumable" | "reusable" | "individual";

interface FieldErrors {
  name?: string;
  categoryId?: string;
  itemType?: string;
  locationId?: string;
  minStock?: string;
  description?: string;
  barcode?: string;
  imgUrl?: string;
  observations?: string;
  form?: string;
}

const ITEM_TYPE_OPTIONS: Array<{ value: ItemType; label: string }> = [
  { value: "consumable", label: "Consumible" },
  { value: "reusable", label: "Reutilizable" },
  { value: "individual", label: "Individual" },
];

interface ImplementEditModalProps {
  implementId: number | null;
  isOpen: boolean;
  onClose: () => void;
  onSaved?: (updated: ImplementDetail) => void | Promise<void>;
}

export function ImplementEditModal({ implementId, isOpen, onClose, onSaved }: ImplementEditModalProps) {
  const [loadingImplement, setLoadingImplement] = useState(false);
  const [implement, setImplement] = useState<ImplementDetail | null>(null);
  const [saving, setSaving] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  const [name, setName] = useState("");
  const [categoryIdRaw, setCategoryIdRaw] = useState<string>("");
  const [itemTypeRaw, setItemTypeRaw] = useState<ItemType | "">("");
  const [locationIdRaw, setLocationIdRaw] = useState<string>("");
  const [description, setDescription] = useState("");
  const [barcode, setBarcode] = useState("");
  const [imgUrl, setImgUrl] = useState("");
  const [minStockRaw, setMinStockRaw] = useState("");
  const [observations, setObservations] = useState("");

  const [categories, setCategories] = useState<ActiveCategoryOption[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [categoriesError, setCategoriesError] = useState<string | null>(null);

  const [locations, setLocations] = useState<LocationOption[]>([]);
  const [loadingLocations, setLoadingLocations] = useState(false);
  const [locationsError, setLocationsError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen || implementId == null) {
      return;
    }

    setFieldErrors({});
    setSaving(false);

    setLoadingImplement(true);
    setImplement(null);

    fetchImplementById(implementId)
      .then((detail) => {
        setImplement(detail);
        setName(detail.name ?? "");
        setCategoryIdRaw(detail.categoryId == null ? "" : String(detail.categoryId));
        setItemTypeRaw(detail.item_type ?? "");
        setLocationIdRaw(detail.locationId == null ? "" : String(detail.locationId));
        setDescription(detail.description ?? "");
        setBarcode(detail.barcode ?? "");
        setImgUrl(detail.img_url ?? "");
        setMinStockRaw(detail.min_stock == null ? "" : String(detail.min_stock));
        setObservations(detail.observations ?? "");
      })
      .catch((error) => {
        setFieldErrors({
          form: getErrorMessage(error, "No se pudo cargar el implemento."),
        });
      })
      .finally(() => setLoadingImplement(false));
  }, [implementId, isOpen]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setCategories([]);
    setCategoriesError(null);
    setLoadingCategories(true);

    setLocations([]);
    setLocationsError(null);
    setLoadingLocations(true);

    fetchActiveCategories()
      .then((result) => setCategories(result))
      .catch((error) => setCategoriesError(getErrorMessage(error, "No se pudo cargar las categorias.")))
      .finally(() => setLoadingCategories(false));

    fetchLocations()
      .then((result) => setLocations(result))
      .catch((error) => setLocationsError(getErrorMessage(error, "No se pudo cargar las ubicaciones.")))
      .finally(() => setLoadingLocations(false));
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }
    document.body.classList.add("modal-open");
    return () => {
      document.body.classList.remove("modal-open");
    };
  }, [isOpen]);

  const isCategoryDisabled = useMemo(
    () => loadingCategories || Boolean(categoriesError) || categories.length === 0,
    [categories.length, categoriesError, loadingCategories],
  );

  const isLocationDisabled = useMemo(
    () => loadingLocations || Boolean(locationsError) || locations.length === 0,
    [loadingLocations, locations.length, locationsError],
  );

  const currentCategoryInactive = useMemo(() => {
    if (!implement?.category) {
      return false;
    }
    return !implement.category.active;
  }, [implement?.category]);

  const isUsingInactiveCategory = useMemo(() => {
    if (!currentCategoryInactive) {
      return false;
    }
    const currentId = implement?.category?.id;
    if (currentId == null) {
      return false;
    }
    return categoryIdRaw.trim() === String(currentId);
  }, [categoryIdRaw, currentCategoryInactive, implement?.category?.id]);

  const categoryInactiveError = useMemo(() => {
    if (!currentCategoryInactive) {
      return null;
    }
    if (!isUsingInactiveCategory) {
      return null;
    }
    return "La categoria actual esta inactiva. Debes seleccionar una categoria activa para guardar.";
  }, [currentCategoryInactive, isUsingInactiveCategory]);

  const inactiveCategoryOption = useMemo(() => {
    if (!currentCategoryInactive || !implement?.category) {
      return null;
    }
    return {
      id: implement.category.id,
      name: implement.category.name,
    };
  }, [currentCategoryInactive, implement?.category]);

  if (!isOpen) {
    return null;
  }

  function validateClientSide(): FieldErrors {
    const errors: FieldErrors = {};
    const categoryId = categoryIdRaw.trim() ? Number(categoryIdRaw) : null;
    const locationId = locationIdRaw.trim() ? Number(locationIdRaw) : NaN;
    const minStock = minStockRaw.trim() ? Number(minStockRaw) : NaN;

    if (name.trim().length === 0) {
      errors.name = "El nombre es obligatorio.";
    }
    if (name.trim().length > 150) {
      errors.name = "El nombre no puede superar 150 caracteres.";
    }
    if (itemTypeRaw.trim().length === 0) {
      errors.itemType = "El tipo de implemento es obligatorio.";
    }
    if (!Number.isFinite(locationId)) {
      errors.locationId = "La ubicacion es obligatoria.";
    }
    if (!Number.isFinite(minStock) || minStock <= 0 || !Number.isInteger(minStock)) {
      errors.minStock = "El stock minimo debe ser un entero positivo.";
    }
    if (description.trim().length > 2000) {
      errors.description = "La descripcion no puede superar 2000 caracteres.";
    }
    if (observations.trim().length > 500) {
      errors.observations = "Las observaciones no pueden superar 500 caracteres.";
    }
    if (currentCategoryInactive) {
      if (categoryId == null || !Number.isFinite(Number(categoryId))) {
        errors.categoryId = "Debes seleccionar una categoria activa.";
      } else if (isUsingInactiveCategory) {
        errors.categoryId = categoryInactiveError ?? "Debes seleccionar una categoria activa.";
      }
    }

    return errors;
  }

  function mapApiErrorToFields(message: string): FieldErrors {
    const normalized = message.toLowerCase();
    const errors: FieldErrors = {};

    if (normalized.includes("nombre")) {
      errors.name = message;
      return errors;
    }
    if (normalized.includes("categoria")) {
      errors.categoryId = message;
      return errors;
    }
    if (normalized.includes("tipo")) {
      errors.itemType = message;
      return errors;
    }
    if (normalized.includes("ubicacion")) {
      errors.locationId = message;
      return errors;
    }
    if (normalized.includes("stock minimo")) {
      errors.minStock = message;
      return errors;
    }
    if (normalized.includes("descripcion")) {
      errors.description = message;
      return errors;
    }
    if (normalized.includes("barra")) {
      errors.barcode = message;
      return errors;
    }
    if (normalized.includes("imagen") || normalized.includes("url")) {
      errors.imgUrl = message;
      return errors;
    }
    if (normalized.includes("observaciones")) {
      errors.observations = message;
      return errors;
    }

    errors.form = message;
    return errors;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFieldErrors({});

    if (implementId == null) {
      setFieldErrors({ form: "No se pudo identificar el implemento." });
      return;
    }

    const clientErrors = validateClientSide();
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      return;
    }

    const categoryId = categoryIdRaw.trim() ? Number(categoryIdRaw) : null;
    const locationId = Number(locationIdRaw);
    const minStock = Number(minStockRaw);

    setSaving(true);
    try {
      const updated = await updateImplement(implementId, {
        name: name.trim(),
        category_id: Number.isFinite(Number(categoryId)) ? categoryId : null,
        location_id: locationId,
        item_type: itemTypeRaw as ItemType,
        description: description.trim() ? description.trim() : null,
        barcode: barcode.trim() ? barcode.trim() : null,
        img_url: imgUrl.trim() ? imgUrl.trim() : null,
        min_stock: minStock,
        observations: observations.trim() ? observations.trim() : null,
      });

      setImplement(updated);
      if (onSaved) {
        await onSaved(updated);
      }
      onClose();
    } catch (error) {
      const payload = getApiErrorPayload(error);
      const message = payload?.message ?? getErrorMessage(error, "No se pudo actualizar el implemento.");
      setFieldErrors(mapApiErrorToFields(message));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal modal--wide">
        <h3>Editar producto</h3>
        <p>Actualiza los datos del producto.</p>

        {fieldErrors.form ? <p className="field-error">{fieldErrors.form}</p> : null}
        {loadingImplement ? <p className="field-hint">Cargando información...</p> : null}

        <form onSubmit={handleSubmit} className="modal-form">
          <section className="modal-form-section">
            <h4>Información principal</h4>
            <div className="modal-form-grid">
              <div className="modal-field modal-field--full">
                <label htmlFor="implement-edit-name">Nombre</label>
                <input
                  id="implement-edit-name"
                  value={name}
                  onChange={(event) => {
                    setName(event.target.value);
                    setFieldErrors((current) => ({ ...current, name: undefined }));
                  }}
                  placeholder="Ej: Guantes latex"
                  maxLength={150}
                  required
                />
                {fieldErrors.name ? <p className="field-error">{fieldErrors.name}</p> : null}
              </div>

              <div className="modal-field">
                <label htmlFor="implement-edit-category">Categoria</label>
                {currentCategoryInactive ? (
                  <p className="field-hint">
                    Categoría actual: <span className="badge badge--inactive">{implement?.category?.name ?? "Categoría"} (inactiva)</span>
                  </p>
                ) : null}
                <select
                  id="implement-edit-category"
                  value={categoryIdRaw}
                  onChange={(event) => {
                    setCategoryIdRaw(event.target.value);
                    setFieldErrors((current) => ({ ...current, categoryId: undefined }));
                  }}
                  disabled={isCategoryDisabled}
                >
                  {inactiveCategoryOption ? (
                    <option value={String(inactiveCategoryOption.id)} disabled>
                      {inactiveCategoryOption.name} [Inactiva]
                    </option>
                  ) : null}
                  <option value="">Sin categoria</option>
                  {categories.map((category) => (
                    <option key={category.id} value={String(category.id)}>
                      {category.name}
                    </option>
                  ))}
                </select>
                {categoriesError ? <p className="field-error">{categoriesError}</p> : null}
                {fieldErrors.categoryId ? <p className="field-error">{fieldErrors.categoryId}</p> : null}
              </div>

              <div className="modal-field">
                <label htmlFor="implement-edit-item-type">Tipo de implemento</label>
                <select
                  id="implement-edit-item-type"
                  value={itemTypeRaw}
                  onChange={(event) => {
                    setItemTypeRaw(event.target.value as ItemType | "");
                    setFieldErrors((current) => ({ ...current, itemType: undefined }));
                  }}
                  required
                >
                  <option value="" disabled>
                    Selecciona un tipo
                  </option>
                  {ITEM_TYPE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
                {fieldErrors.itemType ? <p className="field-error">{fieldErrors.itemType}</p> : null}
              </div>

              <div className="modal-field modal-field--full">
                <label htmlFor="implement-edit-location">Ubicacion</label>
                <select
                  id="implement-edit-location"
                  value={locationIdRaw}
                  onChange={(event) => {
                    setLocationIdRaw(event.target.value);
                    setFieldErrors((current) => ({ ...current, locationId: undefined }));
                  }}
                  disabled={isLocationDisabled}
                  required
                >
                  <option value="" disabled>
                    Selecciona una ubicacion
                  </option>
                  {locations.map((location) => (
                    <option key={location.id} value={String(location.id)}>
                      {location.name}
                    </option>
                  ))}
                </select>
                {locationsError ? <p className="field-error">{locationsError}</p> : null}
                {fieldErrors.locationId ? <p className="field-error">{fieldErrors.locationId}</p> : null}
              </div>

              <div className="modal-field modal-field--full">
                <label htmlFor="implement-edit-description">Descripcion</label>
                <textarea
                  id="implement-edit-description"
                  value={description}
                  onChange={(event) => {
                    setDescription(event.target.value);
                    setFieldErrors((current) => ({ ...current, description: undefined }));
                  }}
                  maxLength={2000}
                  placeholder="Opcional"
                />
                {fieldErrors.description ? <p className="field-error">{fieldErrors.description}</p> : null}
              </div>
            </div>
          </section>

          <section className="modal-form-section">
            <h4>Inventario y trazabilidad</h4>
            <div className="modal-form-grid">
              <div className="modal-field">
                <label htmlFor="implement-edit-min-stock">Stock minimo</label>
                <input
                  id="implement-edit-min-stock"
                  type="number"
                  min={1}
                  step={1}
                  value={minStockRaw}
                  onChange={(event) => {
                    setMinStockRaw(event.target.value);
                    setFieldErrors((current) => ({ ...current, minStock: undefined }));
                  }}
                  required
                />
                {fieldErrors.minStock ? <p className="field-error">{fieldErrors.minStock}</p> : null}
              </div>

              <div className="modal-field">
                <label htmlFor="implement-edit-barcode">Codigo de barras</label>
                <input
                  id="implement-edit-barcode"
                  value={barcode}
                  onChange={(event) => {
                    setBarcode(event.target.value);
                    setFieldErrors((current) => ({ ...current, barcode: undefined }));
                  }}
                  maxLength={100}
                  placeholder="Opcional"
                />
                {fieldErrors.barcode ? <p className="field-error">{fieldErrors.barcode}</p> : null}
              </div>

              <div className="modal-field modal-field--full">
                <label htmlFor="implement-edit-img-url">URL de imagen</label>
                <input
                  id="implement-edit-img-url"
                  value={imgUrl}
                  onChange={(event) => {
                    setImgUrl(event.target.value);
                    setFieldErrors((current) => ({ ...current, imgUrl: undefined }));
                  }}
                  maxLength={2000}
                  placeholder="https://..."
                />
                {fieldErrors.imgUrl ? <p className="field-error">{fieldErrors.imgUrl}</p> : null}
              </div>

              <div className="modal-field modal-field--full">
                <label htmlFor="implement-edit-observations">Observaciones</label>
                <textarea
                  id="implement-edit-observations"
                  value={observations}
                  onChange={(event) => {
                    setObservations(event.target.value);
                    setFieldErrors((current) => ({ ...current, observations: undefined }));
                  }}
                  maxLength={500}
                  placeholder="Opcional"
                />
                {fieldErrors.observations ? <p className="field-error">{fieldErrors.observations}</p> : null}
              </div>
            </div>
          </section>

          <div className="modal-actions">
            <button type="button" className="button button--ghost" onClick={onClose} disabled={saving}>
              Cancelar
            </button>
            <button
              type="submit"
              className="button"
              disabled={
                saving ||
                loadingImplement ||
                name.trim().length === 0 ||
                itemTypeRaw.trim().length === 0 ||
                locationIdRaw.trim().length === 0 ||
                (currentCategoryInactive && (categoryIdRaw.trim().length === 0 || isUsingInactiveCategory)) ||
                Boolean(categoriesError) ||
                Boolean(locationsError)
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
