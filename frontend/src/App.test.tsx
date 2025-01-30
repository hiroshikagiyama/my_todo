import '@testing-library/jest-dom/vitest';
import {describe, expect, test, vitest, afterEach} from 'vitest'
import {cleanup, render, screen, waitFor} from "@testing-library/react";
import App from "./App.tsx";
import {userEvent} from "@testing-library/user-event";

const spyFetchGetJson = vitest.hoisted(() => vitest.fn())
const mockFetch = vitest.fn().mockResolvedValue({json: spyFetchGetJson})
vitest.stubGlobal("fetch", mockFetch)

describe("App Tests", () => {

    afterEach(() => {
        cleanup()
    });

    test('given data is not existed when renders app then see only title and button', () => {
        spyFetchGetJson.mockResolvedValue([])

        render(<App/>)

        expect(screen.getByText("My Todo")).toBeInTheDocument()
        expect(screen.getByRole("textbox")).toBeInTheDocument()
        expect(screen.getByRole("button", {name: "追加"})).toBeInTheDocument()
    })

    test("given data is existed when render then see todo items", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "todo1", isCompleted: false}])

        render(<App/>)

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo")
            expect(screen.getByText("todo1")).toBeInTheDocument()
        })
    })

    test("when write new item and click button then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.click(screen.getByRole("button", {name: "追加"}))

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })

    test("when data is existed but completed status when render then cannot see todo item", async () => {
        spyFetchGetJson.mockResolvedValue([
            {id: "1", content: "Hello World", isCompleted: true},
            {id: "2", content: "Jun desu", isCompleted: false}
        ])

        render(<App/>)

        // await waitFor(() => {
        //     expect(screen.getByText("Jun desu")).toBeInTheDocument()
        //     expect(screen.queryByText("Hello World")).not.toBeInTheDocument()
        // })
        expect(await screen.findByText("Jun desu")).toBeInTheDocument()
        expect(screen.queryByText("Hello World")).not.toBeInTheDocument()
    })

    test("when write new item press enter then fetch post request", async () => {
        spyFetchGetJson.mockResolvedValue([{id: "1", content: "Hello World", isCompleted: false}])

        render(<App/>)

        await userEvent.type(screen.getByRole("textbox"), "Hello World")
        await userEvent.keyboard('{Enter}')

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith("/api/todo", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({content: "Hello World"}),
            })
            expect(screen.getByText("Hello World")).toBeInTheDocument()
            expect(screen.getByRole("textbox")).toHaveValue("")
        })
    })

})

// query function -> getBy-, findBy-, queryBy-...
// getBy- 1. data O -> return DOM object, data X -> throw exception
// queryBy- 1. data O -> return DOM object, data X -> return null
// findBy - 1. data O -> return Promise(DOM), data X -> throw exception