import '@testing-library/jest-dom/vitest';
import { vitest,expect, test } from 'vitest'
import {render, screen} from "@testing-library/react";
import App from "./App.tsx";

const stubFetch = vitest.hoisted(() => vitest.fn())
vitest.stubGlobal("fetch", () => ({json: stubFetch}))

test('it renders app', () => {
    stubFetch.mockResolvedValue([])

    render(<App />)
    screen.debug()
    expect(screen.getByText("My Todo")).toBeInTheDocument()
})