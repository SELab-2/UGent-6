
describe('landing page', () => {
    beforeEach(() => {
        cy.visit('http://localhost:3001')
    })

    it('contains correct classes', () => {
        // Een '.' betekent dat je een element selecteert op basis van html class
        cy.get(".landing-page")
            .should("exist")
            .find(".landing-page-btn")
            .should("exist")

        cy.contains('Pigeonhole')
            .should('exist')

        cy.get('.navbar')
            .should('exist')
            .find(".landing-page-btn")
            .should("exist")
    })

    it('contains all the cute logos', () => {
        cy.get(".landing-page")
            .find(".logo-item, .ugent-logo, .code-logo, .js-logo, .docker-logo, .py-logo, .c-logo, .blob-image")
            .should("exist")
            .and("be.visible")

    })

    it('has a title and subtitle', () => {
        cy.get('.landing-title').should('exist');
        cy.contains('UGent').should('exist')
        cy.contains('projecten').should('exist')
    });


    it('can navigate to the login page', () => {
        cy.get('.landing-page-btn').first().click()

        // Controleer of het nieuwe venster is geopend
        cy.window().should('exist')
    });

    it('is responsive', () => {
        //test de pagina op een kleiner scherm. De logos en afbeeldingen zijn niet meer zichtbaar dan
        cy.viewport(800, 550)
        cy.get('.landing-page').should('exist')
            .find(".logo-item, .ugent-logo, .code-logo, .js-logo, .docker-logo, .py-logo, .c-logo, .blob-image")
            .should("exist")
            .and("not.be.visible")
    });
})