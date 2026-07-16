/*
 * Copyright (C) 2026 neossoftware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * @author neossoftware
 */
package com.nimbusframework.web;

/**
 * Contenedor que agrupa el nombre lógico de la vista y los atributos del modelo
 * en un único objeto de retorno del controlador.
 *
 * Uso equivalente al de Spring MVC:
 * <pre>
 *   public ModelAndView home(...) {
 *       return new ModelAndView("home")
 *               .addObject("titulo", "Página Principal")
 *               .addObject("usuario", user);
 *   }
 * </pre>
 *
 * También soporta el prefijo "redirect:":
 * <pre>
 *   return new ModelAndView("redirect:/home.do");
 * </pre>
 */
public class ModelAndView {

    private final String viewName;
    private final Model  model;

    /** @param viewName el nombre lógico de la vista, o "redirect:..." para redirigir. */
    public ModelAndView(String viewName) {
        this.viewName = viewName;
        this.model    = new Model();
    }

    /** Agrega un atributo al modelo. */
    public ModelAndView addObject(String name, Object value) {
        model.addAttribute(name, value);
        return this;
    }

    /** @return el nombre lógico de la vista. */
    public String getViewName() { return viewName; }
    /** @return el modelo con los atributos agregados. */
    public Model  getModel()    { return model; }
}
